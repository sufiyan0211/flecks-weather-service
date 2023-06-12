package com.flecks.model;

import lombok.Data;

/**
 * <p>
 *     This class is basically a Model class for Player.
 * </p>
 * @author sofiyan
 */
@Data
public class Player {
    private String id;
    private String username;
    private String email;
    private String password;
    private String city;
}
