package org.example.migration;

import java.lang.reflect.InvocationTargetException;

public class Main {

    public static void main(String[] args) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, InstantiationException {
        Teste teste = new Teste();

        System.out.println(Schema.getInstance().getAllFromSql(teste));

    }
}