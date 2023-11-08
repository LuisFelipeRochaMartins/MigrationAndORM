package org.orm.migration;

import org.orm.migration.annotations.Table;

public class Main {

    public static void main(String[] args) {
        var endereco = new Endereco();
        var pessoa   = new Pessoa();
//
//        pessoa.drop();
//        endereco.drop();
//
//        pessoa.create();
//        endereco.create();

//        Schema.getInstance().addForeignKey(pessoa);
//        Schema.getInstance().addForeignKey(endereco);

        pessoa.setId(1L);
        pessoa.setNome("Luis");
        pessoa.setCpf("106.954.919-38");

        endereco.setId(3L);
        endereco.setBairro("Cristian");
        endereco.setRua("Cristian");

        pessoa.setEndereco(endereco);

        endereco.save();
        pessoa.update();

    }
}