// Envia el formulario del colaborador (publicar o editar un juego) por XHR en vez de dejar que
// el navegador lo envie y recargue la pagina. Se hace con XMLHttpRequest y no con fetch porque
// solo XHR da el progreso de SUBIDA (xhr.upload.onprogress).
//
// Lo que se gana: barra de progreso, boton de cancelar, aviso si intentas salir a mitad y que
// un error de validacion no te haga volver a elegir el ZIP, que con un envio normal se pierde.
// Lo que NO se gana: si el usuario sale de la pagina, la subida se corta igualmente.
//
// Sin JS el formulario sigue funcionando como siempre: el controlador solo responde JSON si la
// peticion trae la cabecera X-Requested-With.
(function () {

    var form = document.querySelector('form[data-subida]');

    if (!form || !window.XMLHttpRequest || !window.FormData) {
        return;
    }

    var boton = form.querySelector('button[type="submit"]');
    var xhr = null;
    var subiendo = false;
    // Verdadero cuando ya llegaron todos los bytes al servidor. A partir de ahi cancelar no
    // sirve: el servidor ya tiene la peticion entera y la procesa igualmente
    var subidaCompleta = false;

    // Los textos vienen de la plantilla (data-txt-*) para que salgan en el idioma del usuario
    function texto(clave) {
        return form.getAttribute('data-txt-' + clave) || '';
    }

    // Panel de progreso, justo encima de la fila de botones. Se crea aqui para no repetir el
    // mismo HTML en las dos plantillas
    var panel = document.createElement('div');
    panel.className = 'subida-panel';
    panel.hidden = true;
    panel.innerHTML =
        '<div class="subida-linea"><span class="subida-estado"></span><span class="subida-pct"></span></div>' +
        '<progress class="subida-barra" max="100" value="0"></progress>' +
        '<button type="button" class="btn btn-sm subida-cancelar"></button>';

    var estado = panel.querySelector('.subida-estado');
    var pct = panel.querySelector('.subida-pct');
    var barra = panel.querySelector('.subida-barra');
    var cancelar = panel.querySelector('.subida-cancelar');
    cancelar.textContent = texto('cancelar');

    var filaBotones = boton ? boton.closest('.row') : null;

    if (filaBotones) {
        filaBotones.parentNode.insertBefore(panel, filaBotones);
    } else {
        form.appendChild(panel);
    }

    // Aviso de error: se reutiliza el de la pagina si ya habia uno (el del primer envio fallido)
    function mostrarError(mensaje) {

        var aviso = document.querySelector('.alert.alert-err');

        if (!aviso) {
            aviso = document.createElement('div');
            aviso.className = 'alert alert-err';
            form.parentNode.insertBefore(aviso, form);
        }
        aviso.textContent = mensaje;
        aviso.scrollIntoView({behavior: 'smooth', block: 'center'});
    }

    function megas(bytes) {
        return (bytes / 1048576).toFixed(1).replace('.', ',');
    }

    function terminar() {
        subiendo = false;
        xhr = null;
        panel.hidden = true;
        barra.value = 0;
        pct.textContent = '';

        if (boton) {
            boton.disabled = false;
        }
    }

    // Si hay una subida en curso, el navegador pregunta antes de dejar la pagina
    window.addEventListener('beforeunload', function (e) {

        if (subiendo) {
            e.preventDefault();
            e.returnValue = texto('salir');
            return texto('salir');
        }
    });

    cancelar.addEventListener('click', function () {

        if (xhr && !subidaCompleta) {
            xhr.abort();
        }
    });

    form.addEventListener('submit', function (e) {

        // El submit solo llega aqui si el navegador ya valido los campos required
        e.preventDefault();

        if (subiendo) {
            return;
        }

        subiendo = true;
        subidaCompleta = false;
        xhr = new XMLHttpRequest();
        xhr.open('POST', form.action);
        xhr.setRequestHeader('X-Requested-With', 'XMLHttpRequest');
        xhr.setRequestHeader('Accept', 'application/json');

        panel.hidden = false;
        estado.textContent = texto('subiendo');
        cancelar.hidden = false;

        if (boton) {
            boton.disabled = true;
        }

        xhr.upload.onprogress = function (ev) {

            if (!ev.lengthComputable) {
                return;
            }

            var porcentaje = Math.floor(ev.loaded / ev.total * 100);
            barra.value = porcentaje;
            pct.textContent = porcentaje + '% · ' + megas(ev.loaded) + ' / ' + megas(ev.total) + ' MB';
        };

        // El 100% de la subida no es el final: el servidor todavia valida y guarda el ZIP.
        // Sin este mensaje parece que la pagina se ha colgado
        xhr.upload.onload = function () {
            subidaCompleta = true;
            estado.textContent = texto('procesando');
            cancelar.hidden = true;
        };

        xhr.onload = function () {

            var respuesta = null;

            try {
                respuesta = JSON.parse(xhr.responseText);
            } catch (err) {
                respuesta = null;
            }

            if (xhr.status >= 200 && xhr.status < 300 && respuesta && respuesta.ok) {
                // Ya no hay nada que perder: se quita el aviso antes de salir
                subiendo = false;
                window.location.href = respuesta.redirect;
                return;
            }

            terminar();

            // Un error nuestro trae mensaje; cualquier otra cosa (sesion caducada, archivo mas
            // grande que el limite, caida del servidor) no es JSON nuestro y sale el generico
            mostrarError(respuesta && respuesta.error ? respuesta.error : texto('error'));
        };

        xhr.onerror = function () {
            terminar();
            mostrarError(texto('error'));
        };

        xhr.onabort = function () {
            terminar();
            mostrarError(texto('cancelada'));
        };

        xhr.send(new FormData(form));
    });
})();
