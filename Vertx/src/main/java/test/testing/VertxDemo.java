package test.testing;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.jdbc.JDBCClient;
import io.vertx.ext.sql.ResultSet;
import io.vertx.ext.sql.SQLClient;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;


public class VertxDemo {

    public static void main(String[] args) {
        System.out.println("demo vert.x");

        Vertx vertx = Vertx.vertx();

        Router router = Router.router(vertx);

        router.route().handler(BodyHandler.create());
        JsonObject dbconfig = new JsonObject();
        dbconfig.put("provider_class", "io.vertx.ext.jdbc.spi.impl.HikariCPDataSourceProvider");
        dbconfig.put("jdbcUrl", "jdbc:postgresql://localhost:5432/test");
        dbconfig.put("driveClassName", "org.postgresql.Driver");
        dbconfig.put("maximumPoolSize", 10);
        dbconfig.put("username", "postgres");
        dbconfig.put("password", "superman123");

        SQLClient client = JDBCClient.createShared(vertx, dbconfig);



        router.post("/users").handler(ctx -> {


            JsonObject json = ctx.getBodyAsJson();
            System.out.println("json body: " + json);

            String name = json.getString("name");
            Integer age = json.getInteger("age");

            client.queryWithParams("insert into users (name,age) values (?, ?)", new JsonArray().add(name).add(age), res2 -> {
                System.out.println("222");


                if (res2.succeeded()) {
                    System.out.println("Данные были отправлены в БД");
                    System.out.println("333");
                    ctx.response().setStatusCode(200).end("Пользователь сохранен");

                } else if (res2.failed()) {

                    System.out.println("Ошибка!");
                    ctx.response().setStatusCode(406).end("Нельзя вводить одно и тоже имя");


                }
            });
            System.out.println("333");
        });
        router.get("/users").handler(ctx -> {
            System.out.println("users called");
            ctx.response()
                    .putHeader("x-custom-key", "demo-value")
                    .putHeader("Content-Type", "application/json");

            client.query(("select name, age from users"), res -> {
                if (res.succeeded()) {
                    ResultSet rs = res.result();

                    var array = new JsonArray();

                    rs.getRows().forEach(array::add);

                    ctx.response().setStatusCode(200).end(array.encodePrettily());
                } else if (res.failed()) {
                    ctx.response().setStatusCode(500).end("Error DB");
                }
            });
        });




            vertx.createHttpServer()
                    .requestHandler(router)
                    .listen(8087, lr -> {
                        System.out.println("server started: " + lr.succeeded());

                        if (lr.failed()) {
                            lr.cause().printStackTrace();
                        }
                    });





    }
}


