package model;


import java.time.LocalDate;

public class Promotion {
    private int id;
    private String codeCoupon;
    private double prixNouv;
    private LocalDate startDate;
    private LocalDate endDate;

    public Promotion() {}

    public Promotion(int id, String codeCoupon, double prixNouv, LocalDate startDate, LocalDate endDate) {
        this.id = id;
        this.codeCoupon = codeCoupon;
        this.prixNouv = prixNouv;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getCodeCoupon() { return codeCoupon; }
    public void setCodeCoupon(String codeCoupon) { this.codeCoupon = codeCoupon; }

    public double getPrixNouv() { return prixNouv; }
    public void setPrixNouv(double prixNouv) { this.prixNouv = prixNouv; }

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }
}