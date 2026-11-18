// Avisos en tiempo real del panel.
// 1) Se conecta al servidor por WebSocket (puerta /ws)
// 2) Por cada campanita de la pagina, se suscribe al canal que indica su data-canal
// 3) Cuando llega un mensaje a ese canal, suma 1 al numero de esa campanita
(function () {
    const campanas = document.querySelectorAll('.campana[data-canal]');

    // Si esta pagina no tiene campanitas (otro rol), no hacemos nada
    if (campanas.length === 0) {
        return;
    }

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