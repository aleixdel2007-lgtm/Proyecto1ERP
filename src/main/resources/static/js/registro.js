// Comprueba la contraseña en el navegador, antes de enviar el formulario.
// setCustomValidity pone el mensaje en el aviso nativo del navegador (el mismo
// que el de la longitud) y bloquea el envio, asi que el formulario no se vacia.
const password = document.getElementById("password");
const repetir = document.getElementById("repetirPassword");

function validarPassword() {
    const valor = password.value;
    let mensaje = "";
    if (valor.length < 8) {
        mensaje = "La contraseña debe tener al menos 8 caracteres.";
    } else if (!/[A-Z]/.test(valor)) {
        mensaje = "La contraseña debe llevar al menos una mayúscula.";
    } else if (!/\d/.test(valor)) {
        mensaje = "La contraseña debe llevar al menos un número.";
    }
    password.setCustomValidity(mensaje); // texto vacio = el campo es valido
}

function validarRepetir() {
    repetir.setCustomValidity(
            repetir.value !== password.value ? "Las contraseñas no coinciden." : "");
}

password.addEventListener("input", function () {
    validarPassword();
    validarRepetir();
});
repetir.addEventListener("input", validarRepetir);

