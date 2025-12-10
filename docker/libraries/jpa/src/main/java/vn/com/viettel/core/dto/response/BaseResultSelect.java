package vn.com.viettel.core.dto.response;

import lombok.Data;

import java.util.List;

@Data
public class BaseResultSelect {
    List<? extends Object> listData;
    Object count;

    public BaseResultSelect() {

    }

    public BaseResultSelect(List<? extends Object> listData, Object count) {
        this.listData = listData;
        this.count = count;
    }
}
