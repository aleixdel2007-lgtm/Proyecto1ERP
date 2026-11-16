package service.implement;

import config.IndiceCiego;
import dao.ClaveLicenciaDao;
import dao.EmpleadoDao;
import dao.VideojuegoDao;
import exception.clave.ClaveNoAnulableException;
import exception.clave.ClaveNoEncontradaException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import model.aprovisionamiento.LineaCompra;
import model.catalogo.Videojuego;
import model.inventario.ClaveLicencia;
import model.inventario.EstadoClave;
import model.seguridad.Empleado;
import model.seguridad.Rol;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import service.interfaces.ClaveLicenciaService;
import service.interfaces.EmailService;

@Service
@Transactional(readOnly = true)
public class ClaveLicenciaServiceImpl implements ClaveLicenciaService {

    private static final int LARGO_MOTIVO = 255;

    // Sin I, O, 0 ni 1: una clave se copia a mano desde un correo y esos cuatro se confunden
    private static final String ALFABETO = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final int LARGO_BLOQUE = 4;
    private static final int INTENTOS_CODIGO = 20;
    private static final SecureRandom ALEATORIO = new SecureRandom();

    @Autowired
    private ClaveLicenciaDao claveLicenciaDao;

    @Autowired
    private VideojuegoDao videojuegoDao;
    @Autowired
    private EmpleadoDao empleadoDao;

    @Autowired
    private EmailService emailService; //el servidor de correo

    // Si quedan estas claves o menos, se avisa a los gestores de inventario
    private static final long UMBRAL_STOCK_BAJO = 3;

    // Las claves del panel. El codigo escrito se convierte en hash antes de bajar al DAO
    @Override
    public List<ClaveLicencia> listarClaves(String codigo, Long videojuegoId, EstadoClave estado) {
        return claveLicenciaDao.listarFiltrado(hashDe(codigo), videojuegoId, estado);
    }

    // Cuantas claves hay en un estado, para los contadores de la cabecera
    @Override
    public long contarPorEstado(EstadoClave estado) {
        return claveLicenciaDao.contarPorEstado(estado);
    }

    // Los videojuegos del desplegable del filtro, por titulo
    @Override
    public List<Videojuego> listarVideojuegos() {
        return videojuegoDao.findAll(Sort.by("titulo"));
    }

    // Una clave con su juego, su cliente y su compra ya cargados, o 404 si no existe
    @Override
    public ClaveLicencia buscarClaveXId(Long id) {

        ClaveLicencia clave = claveLicenciaDao.buscarConDetalle(id).orElse(null);

        if (clave == null) {
            throw new ClaveNoEncontradaException(id);
        }
        return clave;
    }

    // Deja una clave DISPONIBLE inservible para siempre: no se borra la fila, se marca
    @Override
    @Transactional
    public void anularClave(Long id, String motivo) {

        ClaveLicencia clave = buscarClaveXId(id);

        // El unico guardia de la transicion: la base no impide ningun cambio de estado
        if (clave.getEstado() != EstadoClave.DISPONIBLE) {
            throw new ClaveNoAnulableException(clave.getEstado());
        }

        if (motivo == null || motivo.isBlank()) {
            throw new IllegalArgumentException("claves.error.motivoObligatorio");
        }

        if (motivo.trim().length() > LARGO_MOTIVO) {
            // El limite tambien esta escrito en claves.error.motivoLargo: si cambia LARGO_MOTIVO,
            // hay que tocar los tres .properties
            throw new IllegalArgumentException("claves.error.motivoLargo");
        }

        clave.setEstado(EstadoClave.ANULADA);
        clave.setFechaAnulacion(LocalDateTime.now());
        clave.setMotivoAnulacion(motivo.trim());

        claveLicenciaDao.save(clave);
    }

    // El codigo se busca por su hash, nunca por la columna cifrada; vacio apaga el filtro
    private String hashDe(String codigo) {

        if (codigo == null || codigo.isBlank()) {
            return null;
        }
        return IndiceCiego.hash(codigo);
    }

    @Override
    public void avisarSiQuedaPocoStock(Videojuego videojuego) {

        long quedan = claveLicenciaDao.contarPorVideojuegoYEstado(
                videojuego.getId(), EstadoClave.DISPONIBLE);

        if (quedan > UMBRAL_STOCK_BAJO) {
            return;
        }

        List<Empleado> gestores = empleadoDao.listarFiltrado(null, null, Rol.GESTOR_INVENTARIO);

        for (Empleado gestor : gestores) {
            emailService.enviarAvisoStockBajo(gestor.getEmail(), videojuego.getTitulo(), quedan);
        }
    }

    // La unica via por la que entra stock comprado. El codigo se cifra solo al guardar
    // (CifradoAesConverter), pero el hash NO se calcula solo: se pone aqui, igual que en
    // DatosIniciales. La fecha de alta la pone el @PrePersist de la entidad
    @Override
    @Transactional
    public int acunarClaves(LineaCompra linea, int cantidad) {

        if (cantidad < 0) {
            throw new IllegalArgumentException("claves.error.unidadesNegativas");
        }

        if (cantidad == 0) {
            return 0;
        }

        Videojuego videojuego = linea.getVideojuego();
        List<ClaveLicencia> nuevas = new ArrayList<>();
        Set<String> hashesDelLote = new HashSet<>();

        for (int i = 0; i < cantidad; i++) {

            String codigo = codigoLibre(videojuego, hashesDelLote);

            ClaveLicencia clave = new ClaveLicencia();
            clave.setVideojuego(videojuego);
            clave.setLineaCompra(linea);
            clave.setEstado(EstadoClave.DISPONIBLE);
            clave.setCodigo(codigo);
            clave.setCodigoHash(IndiceCiego.hash(codigo));

            nuevas.add(clave);
        }

        claveLicenciaDao.saveAll(nuevas);

        return nuevas.size();
    }

    // Un codigo que no tenga ya nadie. Se mira contra la base Y contra lo que lleva generado
    // este mismo lote, que esas claves todavia no han bajado a la tabla
    private String codigoLibre(Videojuego videojuego, Set<String> hashesDelLote) {

        String prefijo = prefijoDe(videojuego.getTitulo());

        for (int intento = 0; intento < INTENTOS_CODIGO; intento++) {

            String codigo = prefijo + "-" + bloque() + "-" + bloque() + "-" + bloque();
            String hash = IndiceCiego.hash(codigo);

            if (hashesDelLote.add(hash) && !claveLicenciaDao.existsByCodigoHash(hash)) {
                return codigo;
            }
            hashesDelLote.remove(hash);
        }

        throw new IllegalStateException(
                "No se ha podido generar un codigo libre para " + videojuego.getTitulo());
    }

    // Las cuatro primeras letras del titulo, en mayusculas y rellenando con X si no llegan:
    // "Eclipse Protocol" -> ECLI. Es solo para que la clave se reconozca de un vistazo
    private String prefijoDe(String titulo) {

        StringBuilder prefijo = new StringBuilder();

        for (char c : titulo.toUpperCase().toCharArray()) {
            if (Character.isLetter(c) && prefijo.length() < LARGO_BLOQUE) {
                prefijo.append(c);
            }
        }

        while (prefijo.length() < LARGO_BLOQUE) {
            prefijo.append('X');
        }
        return prefijo.toString();
    }

    private String bloque() {

        StringBuilder bloque = new StringBuilder(LARGO_BLOQUE);

        for (int i = 0; i < LARGO_BLOQUE; i++) {
            bloque.append(ALFABETO.charAt(ALEATORIO.nextInt(ALFABETO.length())));
        }
        return bloque.toString();
    }
}
