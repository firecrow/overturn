package tech.overturn.crowmail.struct;

import tech.overturn.crowmail.Data;

public class EmailWithType extends Data {
    Integer _id;
    public String name;
    public String email;
    public String type;

    public Integer getId(){
        return _id;
    }
    public void setId(Integer id) {
       this._id = id;
    }
}
