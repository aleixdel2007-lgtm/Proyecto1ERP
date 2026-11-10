// Galeria de la ficha: al pulsar una captura se pone donde va el banner, y pulsando la
// imagen grande se vuelve a la de siempre. No guarda nada en ningun sitio, asi que al
// recargar la pagina Thymeleaf vuelve a pintar el banner y la galeria queda como estaba.
function bindGaleria() {
    const principal = document.getElementById("galeriaPrincipal");
    const miniaturas = document.querySelectorAll(".gal-thumbs .thumb");

    // Sin imagen grande o sin capturas no hay nada que intercambiar
    if (!principal || miniaturas.length === 0) {
        return;
    }

    // La de partida se guarda antes de tocar nada: es la que devuelve el clic en la grande
    const inicial = {
        src: principal.getAttribute("src"),
        alt: principal.getAttribute("alt")
    };

    const mostrar = (src, alt, miniatura) => {
        principal.setAttribute("src", src);
        principal.setAttribute("alt", alt);
        // Solo la pulsada se queda con el anillo morado; con null se apagan todas
        miniaturas.forEach(m => m.classList.toggle("on", m === miniatura));
    };

    miniaturas.forEach(miniatura => {
        const imagen = miniatura.querySelector("img");

        if (!imagen) {
            return;
        }
        miniatura.addEventListener("click", () =>
                mostrar(imagen.getAttribute("src"), imagen.getAttribute("alt"), miniatura));
    });

    principal.addEventListener("click", () => mostrar(inicial.src, inicial.alt, null));

    // La clase la pone el JS y no la plantilla: asi el cursor de mano solo sale cuando
    // el intercambio esta de verdad conectado
    principal.closest(".gal-main").classList.add("interactiva");
}

function enseñarForm() {
    const form = document.querySelector(".form-valoracion");
    const estrellas = document.querySelectorAll(".estrellas input[type='radio']");

    if (!form || estrellas.length === 0) {
        return;
    }

    form.hidden = true;

    estrellas.forEach(estrella => {
        estrella.addEventListener("change", () => {
            form.hidden = false;
        }, { once: true });
    });
}

bindGaleria();
enseñarForm();
