package org.example.migration;

import org.example.migration.annotations.Column;
import org.example.migration.annotations.PrimaryKey;
import org.example.migration.annotations.Table;


@Table(name = "olaLuis")
public class Teste extends ModelORM {

    @PrimaryKey
    private Long id;

    @Column(
            name = "testando",
            lenght = 80
    )
    private String nome;
    private String cpf;
    private Integer age;
    public Teste() {

    }

    public Teste(Long id, String nome, String cpf, Integer age) {
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

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Teste");
        sb.append("  id = ").append(id);
        sb.append(", nome='").append(nome).append('\'');
        sb.append(", cpf='").append(cpf).append('\'');
        sb.append(", age=").append(age);
        return sb.toString();
    }
}
