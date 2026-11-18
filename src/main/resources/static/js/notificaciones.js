// Avisos en tiempo real del panel.
// 1) Cada campanita abre y cierra su desplegable al pulsarla
// 2) Se conecta al servidor por WebSocket (puerta /ws)
// 3) Por cada campanita, se suscribe al canal que indica su data-canal
// 4) Cuando llega un mensaje a ese canal, suma 1 al numero de esa campanita
(function () {
    const campanas = document.querySelectorAll('.campana[data-canal]');

    // Si esta pagina no tiene campanitas (otro rol), no hacemos nada
    if (campanas.length === 0) {
        return;
    }

    // ---- Desplegable ----
    function cerrarMenus() {
        document.querySelectorAll('.campana-menu').forEach(function (menu) {
            menu.hidden = true;
        });
        campanas.forEach(function (campana) {
            campana.setAttribute('aria-expanded', 'false');
        });
    }

    campanas.forEach(function (campana) {
        const menu = campana.parentElement.querySelector('.campana-menu');

        campana.addEventListener('click', function (evento) {
            // Que el clic no llegue al documento, o lo cerraria nada mas abrirlo
            evento.stopPropagation();
            const estabaAbierto = !menu.hidden;
            cerrarMenus();
            menu.hidden = estabaAbierto;
            campana.setAttribute('aria-expanded', String(!estabaAbierto));
        });
    });

    // Un clic fuera, o la tecla Escape, cierran el desplegable
    document.addEventListener('click', cerrarMenus);
    document.addEventListener('keydown', function (evento) {
        if (evento.key === 'Escape') {
            cerrarMenus();
        }
    });

    // ---- Avisos en vivo ----
    function sumarUno(numero) {
        const actual = parseInt(numero.textContent, 10) || 0;
        numero.textContent = actual + 1;
        numero.classList.remove('vacio');
    }

    const protocolo = location.protocol === 'https:' ? 'wss://' : 'ws://';

    // "cliente" aqui es la conexion del navegador, no un Cliente de la tienda
    const cliente = new StompJs.Client({
        brokerURL: protocolo + location.host + '/ws',
        reconnectDelay: 5000
    });

    cliente.onConnect = function () {
        console.log('[avisos] conectado');

        campanas.forEach(function (campana) {
            const canal = campana.dataset.canal;
            const numero = campana.querySelector('.campana-num');

            cliente.subscribe(canal, function (mensaje) {
                console.log('[avisos]', canal, mensaje.body);
                sumarUno(numero);
            });
        });
    };

    cliente.activate();
})();