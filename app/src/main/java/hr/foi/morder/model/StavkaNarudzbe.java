package hr.foi.morder.model;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class StavkaNarudzbe {
    public Integer artikl_id;
    public Integer narudzba_id;
    public double cijena;
    public Integer kolicina;
    public Date vrijeme_narucivanja;
    public double jedinicna_cijena;


    public StavkaNarudzbe() {
    }



    public StavkaNarudzbe(Integer artikl_id, Integer narudzbaId, double cijena, Integer kolicina, Double jedinicna_cijena) {
        this.artikl_id = artikl_id;
        this.narudzba_id = narudzbaId;
        this.cijena = cijena;
        this.kolicina = kolicina;
        this.jedinicna_cijena = jedinicna_cijena;
    }

    public Integer getArtikl_id() {
        return artikl_id;
    }

    public void setArtikl_id(Integer artikl_id) {
        this.artikl_id = artikl_id;
    }

    public Integer getNarudzba_id() {
        return narudzba_id;
    }

    public void setNarudzba_id(Integer narudzba_id) {
        this.narudzba_id = narudzba_id;
    }

    public double getCijena() {
        return cijena;
    }

    public void setCijena(double cijena) {
        this.cijena = cijena;
    }

    public Integer getKolicina() {
        return kolicina;
    }

    public void setKolicina(Integer kolicina) {
        this.kolicina = kolicina;
    }

    public Date getVrijeme_narucivanja() {
        return vrijeme_narucivanja;
    }

    public void setVrijeme_narucivanja(Date vrijeme_narucivanja) {
        this.vrijeme_narucivanja = vrijeme_narucivanja;
    }
    public double getJedinicna_cijena() {
        return jedinicna_cijena;
    }
    public void setJedinicna_cijena(double jedinicna_cijena) {
        this.jedinicna_cijena = jedinicna_cijena;
    }

    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("artikl_id", this.artikl_id);
        result.put("narudzba_id", this.narudzba_id);
        result.put("cijena", this.cijena);
        result.put("kolicina", this.kolicina);
        result.put("jedinicna_cijena",this.jedinicna_cijena);
        return result;
    }
}