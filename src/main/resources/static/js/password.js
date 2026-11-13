
document.querySelectorAll("[data-toggle-pass]").forEach(function (boton) {
    const input = document.getElementById(boton.dataset.togglePass);
    if (!input) {
        return;
    }

    boton.addEventListener("click", function () {
        let mostrar = input.type === "password";
        input.type = mostrar ? "text" : "password";
        boton.setAttribute("aria-pressed", String(mostrar));
        // Los textos llegan en el idioma elegido, desde data-txt-* del boton (los pone la plantilla)
        boton.setAttribute("aria-label", mostrar
                ? (boton.dataset.txtOcultar || "Ocultar contraseña")
                : (boton.dataset.txtMostrar || "Mostrar contraseña"));
        input.focus();
    });
});
