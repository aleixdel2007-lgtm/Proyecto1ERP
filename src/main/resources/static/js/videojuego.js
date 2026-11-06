// Las pestanas de la ficha: el boton pulsado se queda con la clase on y se ensena su panel.
function bindTabs() {
    document.querySelectorAll("[data-tabs]").forEach(barra => {
        const tarjeta = barra.closest(".card") || document;
        barra.querySelectorAll(".tab").forEach(tab => {
            tab.addEventListener("click", () => {
                barra.querySelectorAll(".tab").forEach(t => t.classList.toggle("on", t === tab));
                tarjeta.querySelectorAll("[data-pane]").forEach(p =>
                        p.classList.toggle("on", p.dataset.pane === tab.dataset.tab));
            });
        });
    });
}

// El interruptor del descuento habilita el campo del precio rebajado. Apagado lo deshabilita,
// y un campo deshabilitado no se envia: asi precio_descuento se guarda a NULL.
function bindInterruptores() {
    document.querySelectorAll("[data-enables]").forEach(casilla => {
        const aplicar = () => {
            const frow = document.getElementById(casilla.dataset.enables);
            if (!frow) {
                return;
            }
            frow.classList.toggle("off", !casilla.checked);
            frow.querySelectorAll("input").forEach(i => i.disabled = !casilla.checked);
        };
        casilla.addEventListener("change", aplicar);
        aplicar();
    });
}

// El chip sigue a su casilla. Sin JavaScript la casilla sigue funcionando: solo se pierde el color.
function bindChips() {
    document.querySelectorAll(".chip input[type=checkbox]").forEach(casilla => {
        casilla.addEventListener("change", () => {
            casilla.closest(".chip").classList.toggle("on", casilla.checked);
        });
    });
}

bindTabs();
bindInterruptores();
bindChips();
