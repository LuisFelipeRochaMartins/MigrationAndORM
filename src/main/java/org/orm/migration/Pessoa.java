package org.orm.migration;

import org.orm.migration.annotations.OneToOne;
import org.orm.migration.annotations.PrimaryKey;
import org.orm.migration.annotations.Table;

import java.awt.font.FontRenderContext;

@Table(name = "tbpessoa")
public class Pessoa extends ModelORM {

    @PrimaryKey
    private Long id;

    private String nome;
    private String cpf;
    private Integer age;

    @OneToOne(
            target = Endereco.class,
            column = "id"
    )
    private Endereco endereco;

    public Pessoa() {

    }

    public Pessoa(Long id, String nome, String cpf, Integer age) {
        this.id   = id;
        this.nome = nome;
        this.cpf  = cpf;
        this.age  = age;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getCpf() {
        return cpf;
    }

    public void setCpf(String cpf) {
        this.cpf = cpf;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public Endereco getEndereco() {
        return endereco;
    }

    public void setEndereco(Endereco endereco) {
        this.endereco = endereco;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Teste");
        sb.append("  id = ").append(id);
        sb.append(", nome = '").append(nome).append('\'');
        sb.append(", cpf = '").append(cpf).append('\'');
        sb.append(", age = ").append(age).append('\'');
        sb.append("endereco = ").append(endereco);
        return sb.toString();
    }
}
