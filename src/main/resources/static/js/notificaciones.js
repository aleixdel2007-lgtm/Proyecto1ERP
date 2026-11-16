// Avisos en tiempo real del panel (requisito 3.5). Abre el WebSocket contra /ws y se
// suscribe a los canales para saber cuando pasa algo, sin recargar la pagina
(function () {

    // StompJs viene de stomp.umd.min.js, que se carga justo antes que este archivo
    var cliente = new StompJs.Client({
        // ws:// en http y wss:// en https; la misma direccion desde la que se ha cargado la pagina
        brokerURL: (location.protocol === 'https:' ? 'wss://' : 'ws://') + location.host + '/ws',
        // si se corta la conexion, lo vuelve a intentar cada 5 segundos
        reconnectDelay: 5000
    });

    // Se ejecuta cuando la conexion con el servidor ya esta abierta
    cliente.onConnect = function () {
        console.log('[avisos] conectado');

        // Me apunto al canal /topic/clientes. Cada mensaje que el servidor publique ahi
        // llega a esta funcion
        cliente.subscribe('/topic/clientes', function (mensaje) {
            console.log('[avisos] clientes:', mensaje.body);
        });
    };

    cliente.activate();
})();