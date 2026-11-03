
document.querySelectorAll("[data-toggle-pass]").forEach(function (boton) {
    const input = document.getElementById(boton.dataset.togglePass);
    if (!input) {
        return;
    }

    boton.addEventListener("click", function () {
        let mostrar = input.type === "password";
        input.type = mostrar ? "text" : "password";
        boton.setAttribute("aria-pressed", String(mostrar));
        boton.setAttribute("aria-label", mostrar ? "Ocultar contraseña" : "Mostrar contraseña");
        input.focus();
    });
});
