package ObsluhaUzivatele;

public class User {
    private String jmeno;
    private String prijmeni;
    private String email;
    private String heslo;
    private String telefonniCislo;

    public User(String jmeno, String prijmeni, String email, String heslo, String telefonniCislo) {
        this.jmeno = jmeno;
        this.prijmeni = prijmeni;
        this.email = email;
        this.heslo = heslo;
        this.telefonniCislo = telefonniCislo;
    }

    public String getJmeno() {
        return jmeno;
    }

    public String getPrijmeni() {
        return prijmeni;
    }

    public String getEmail() {
        return email;
    }

    public String getHeslo() {
        return heslo;
    }

    public String getTelefonniCislo() {
        return telefonniCislo;
    }
}
