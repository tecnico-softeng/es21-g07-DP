package pt.ulisboa.tecnico.socialsoftware.tutor.question.dto;

import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.ConnectionItem;

import java.io.Serializable;


public class ConnectionItemDto implements Serializable{

    private Integer id;
    private String content;


    public ConnectionItemDto() {
    }

    public ConnectionItemDto(ConnectionItem option) {
        setContent(option.getContent());
        this.id = option.getId();
    }

    public Integer getId() {
        return id;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }



}