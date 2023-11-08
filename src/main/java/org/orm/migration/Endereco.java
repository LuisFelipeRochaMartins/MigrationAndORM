package org.orm.migration;

import org.orm.migration.annotations.Column;
import org.orm.migration.annotations.OneToOne;
import org.orm.migration.annotations.PrimaryKey;
import org.orm.migration.annotations.Table;

@Table(name = "tbendereco")
public class Endereco extends ModelORM{

    @PrimaryKey
    @Column(name = "enderecoId")
    private Long id;

    private String bairro;
    private String rua;

    public Endereco(Long id) {
        this.id = id;
    }

    public Endereco() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getBairro() {
        return bairro;
    }

    public void setBairro(String bairro) {
        this.bairro = bairro;
    }

    public String getRua() {
        return rua;
    }

    public void setRua(String rua) {
        this.rua = rua;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Endereco{");
        sb.append("id=").append(id);
        sb.append(", bairro='").append(bairro).append('\'');
        sb.append(", rua='").append(rua).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
