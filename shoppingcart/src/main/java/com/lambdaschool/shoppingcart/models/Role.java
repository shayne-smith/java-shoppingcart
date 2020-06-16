package com.lambdaschool.shoppingcart.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "roles")
public class Role
    extends Auditable
{
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long roleid;

    @NotNull
    @Column(nullable = false,
        unique = true)
    private String name;

    @OneToMany(mappedBy = "role",
        cascade = CascadeType.ALL)
    @JsonIgnoreProperties(value = "role",
        allowSetters = true)
    private List<UserRoles> users = new ArrayList<>();

    public Role()
    {
    }

    public Role(String name)
    {
        this.name = name.toUpperCase();
    }

    public long getRoleid()
    {
        return roleid;
    }

    public void setRoleid(long roleid)
    {
        this.roleid = roleid;
    }

    public String getName()
    {
        if (name == null)
        {
            return null;
        } else
        {
            return name.toUpperCase();
        }
    }

    public void setName(String name)
    {
        this.name = name.toUpperCase();
    }

    public List<UserRoles> getUsers()
    {
        return users;
    }

    public void setUsers(List<UserRoles> users)
    {
        this.users = users;
    }

    //    public String getLastModifiedBy()
    //    {
    //        return lastModifiedBy;
    //    }
}
