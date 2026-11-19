// Valida la nueva contraseña en el navegador mientras se escribe.
// Cada regla de la lista se marca en verde al cumplirse y, mientras falte alguna,
// setCustomValidity bloquea el envio con el aviso nativo del navegador.
// El servidor vuelve a comprobarlo todo: esto es solo comodidad para el usuario.
const actual = document.getElementById("passwordActual");
const nueva = document.getElementById("nuevaPassword");
const repetir = document.getElementById("repetirPassword");

// Mensajes en el idioma elegido, desde data-msg-* del formulario (los pone la plantilla)
const textos = nueva.form.dataset;

// Mismas reglas que ControllerLogin. Simbolo = lo que no es letra, numero ni espacio
const reglas = [
    {
        nombre: "min",
        cumple: v => v.length >= 8,
        mensaje: () => textos.msgCorta || "La contraseña debe tener al menos 8 caracteres"
    },
    {
        nombre: "mayuscula",
        cumple: v => /\p{Lu}/u.test(v),
        mensaje: () => textos.msgMayuscula || "La contraseña debe tener al menos una letra mayúscula"
    },
    {
        nombre: "simbolo",
        cumple: v => /[^\p{L}\p{N}\s]/u.test(v),
        mensaje: () => textos.msgSimbolo || "La contraseña debe tener al menos un símbolo especial"
    },
    {
        nombre: "distinta",
        cumple: v => v.length > 0 && v !== actual.value,
        mensaje: () => textos.msgIgual || "La nueva contraseña debe ser distinta de la actual"
    }
];

function validar() {
    const valor = nueva.value;
    let mensaje = "";

    reglas.forEach(function (regla) {
        const ok = regla.cumple(valor);
        const li = document.querySelector('[data-regla="' + regla.nombre + '"]');
        if (li) {
            li.classList.toggle("ok", ok);
        }
        // Se muestra solo el primer fallo, en el orden del array
        if (!ok && !mensaje) {
            mensaje = regla.mensaje();
        }
    });
    nueva.setCustomValidity(mensaje); // texto vacio = el campo es valido

    repetir.setCustomValidity(repetir.value !== valor
            ? (textos.msgNoCoincide || "Las contraseñas nuevas no coinciden") : "");
}

actual.addEventListener("input", validar);
nueva.addEventListener("input", validar);
repetir.addEventListener("input", validar);
