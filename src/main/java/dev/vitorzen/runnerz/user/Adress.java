package dev.vitorzen.runnerz.user;

public record Adress(
        String street,
        String suite,
        String city,
        String zipcode,
        Geo geo
) {
}
