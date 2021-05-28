package pt.ulisboa.tecnico.socialsoftware.tutor.question.dto;

import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.ConnectionLink;

import java.io.Serializable;


public class ConnectionLinkDto implements Serializable{
    private Integer id;
    private Integer origin;
    private Integer destiny;

    public ConnectionLinkDto(){
        
    }
    public ConnectionLinkDto(Integer origin, Integer destiny) {
        this.origin = origin;
        this.destiny = destiny;
    }

    public ConnectionLinkDto(ConnectionLink link) {
        this.origin = link.getOrigin();
        this.destiny = link.getDestiny();
        this.id = link.getId();
    }
    
    public Integer getId() {
        return id;
    }

    public Integer getOrigin() {
        return origin;
    }
    public Integer getDestiny() {
        return destiny;
    }

}