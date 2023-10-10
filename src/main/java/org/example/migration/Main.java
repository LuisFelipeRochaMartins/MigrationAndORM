package org.example.migration;

import org.example.connection.PostgresSQL;

public class Main {

    public static void main(String[] args) {
        Teste teste = new Teste();

        teste.setId(2L);
        System.out.println(teste.exists());
    }
}