package org.orm.migration;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EnderecoTest extends ModelORM {

    @Test
    void testeDropTabelaEndereco() {
        var endereco = new Endereco();
        endereco.drop();
    }
    @Test
    void testeCriaTabelaEndereco() {
        var endereco = new Endereco();
        endereco.create();
    }

    @Test
    void insereTabelaEndereco() {
        var endereco = new Endereco();
        endereco.setId(1L);
        endereco.setBairro("teste");
        endereco.setRua("Curvello");
        endereco.save();
    }

    @Test
    void alteraRegistroEndereco() {
        var endereco = new Endereco();
        endereco.setId(1L);
        endereco.setBairro("Jardim América");
        endereco.update();
    }

    @Test
    void deleteRegistroEndereco() {
        var endereco = new Endereco();
        endereco.setId(1L);
        endereco.delete();
    }

    @Test
    void getAllFromTableEndereco() {
        var endereco = new Endereco();
        System.out.println(endereco.getAllFromSql());
    }

    @Test
    void soutEndereco() {
        var endereco = new Endereco();
        endereco.setId(1L);
        endereco = (Endereco) endereco.refresh();
        System.out.println(endereco);
    }

}