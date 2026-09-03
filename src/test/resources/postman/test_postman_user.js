// Tests de Postman para GET http://localhost:8080/api/user/1
// Se pegan en la pestania "Scripts" -> "After response" del request.

// 1. Validar codigo de estado 200 OK
pm.test("Estado de respuesta es 200 OK", function () {
    pm.response.to.have.status(200);
});

// 2. Validar tipos de datos de las propiedades principales
pm.test("La estructura principal tiene los tipos correctos", function () {
    const json = pm.response.json();
    pm.expect(json.page).to.be.a('number');
    pm.expect(json.per_page).to.be.a('number');
    pm.expect(json.total).to.be.a('number');
    pm.expect(json.total_pages).to.be.a('number');
    pm.expect(json.data).to.be.an('array');
});

// 3. Validar esquema y tipos de datos del listado de usuarios
pm.test("Cada usuario en 'data' contiene los campos obligatorios", function () {
    const json = pm.response.json();

    json.data.forEach((user) => {
        pm.expect(user.id).to.be.a('number');
        pm.expect(user.email).to.be.a('string');
        pm.expect(user.first_name).to.be.a('string');
        pm.expect(user.last_name).to.be.a('string');
        pm.expect(user.avatar).to.be.a('string');
    });
});

// 4. Validar que los IDs dentro del arreglo 'data' sean correlativos empezando desde 1
pm.test("Los IDs de los usuarios coinciden con su posicion indexada (id = index + 1)", function () {
    const json = pm.response.json();
    json.data.forEach((user, index) => {
        pm.expect(user.id).to.eql(index + 1);
    });
});