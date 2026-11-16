(function () {
    var input = document.getElementById("buscador");
    var tarjetas = document.querySelectorAll(".cat-grid .game");
    var vacio = document.getElementById("sinResultados");
    if (!input || !tarjetas.length) return;

    function normalizar(s) {
        return (s || "").toLowerCase().normalize("NFD").replace(/[̀-ͯ]/g, "").trim();
    }

    var textos = Array.prototype.map.call(tarjetas, function (t) {
        var ttl = t.querySelector(".ttl");
        var meta = t.querySelector(".meta");
        return normalizar((ttl ? ttl.textContent : "") + " " + (meta ? meta.textContent : ""));
    });

    function filtrar() {
        var q = normalizar(input.value);
        var visibles = 0;
        tarjetas.forEach(function (t, i) {
            var ok = q === "" || textos[i].indexOf(q) !== -1;
            t.hidden = !ok;
            if (ok) visibles++;
        });
        vacio.hidden = visibles !== 0;
    }

    input.addEventListener("input", filtrar);
    filtrar();
})();
