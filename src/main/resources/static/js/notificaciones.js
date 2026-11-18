// Avisos en tiempo real del panel.
// 1) Se conecta al servidor por WebSocket (puerta /ws)
// 2) Se suscribe al canal que le toca segun la campanita que haya en la pagina
// 3) Cuando llega un mensaje, suma 1 al numero de la campanita
(function () {
    const campana = document.getElementById('campana');
    const numero = document.getElementById('campana-num');

    // Si esta pagina no tiene campanita (otro rol), no hacemos nada
    if (!campana || !numero) {
        return;
    }

    function sumarUno() {
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
        cliente.subscribe('/topic/clientes', function (mensaje) {
            console.log('[avisos] clientes:', mensaje.body);
            sumarUno();
        });
    };

    cliente.activate();
})();