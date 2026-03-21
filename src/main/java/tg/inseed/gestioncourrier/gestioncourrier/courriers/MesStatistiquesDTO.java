package tg.inseed.gestioncourrier.gestioncourrier.courriers;

import java.util.Map;

public class MesStatistiquesDTO {
    private long total;
    private Map<String, Long> parStatut;  // code_statut -> nombre
    private Map<String, Long> parType;    // code_type -> nombre
    private long entrants;
    private long sortants;
    private long enAttente;
    private long enCours;
    private long traites;
    private long archives;
    private long classes;
    private long rejetes;
    private long urgents;

    // Getters et setters
    public long getTotal() { return total; }
    public void setTotal(long total) { this.total = total; }

    public Map<String, Long> getParStatut() { return parStatut; }
    public void setParStatut(Map<String, Long> parStatut) { this.parStatut = parStatut; }

    public Map<String, Long> getParType() { return parType; }
    public void setParType(Map<String, Long> parType) { this.parType = parType; }

    public long getEntrants() { return entrants; }
    public void setEntrants(long entrants) { this.entrants = entrants; }

    public long getSortants() { return sortants; }
    public void setSortants(long sortants) { this.sortants = sortants; }

    public long getEnAttente() { return enAttente; }
    public void setEnAttente(long enAttente) { this.enAttente = enAttente; }

    public long getEnCours() { return enCours; }
    public void setEnCours(long enCours) { this.enCours = enCours; }

    public long getTraites() { return traites; }
    public void setTraites(long traites) { this.traites = traites; }

    public long getArchives() { return archives; }
    public void setArchives(long archives) { this.archives = archives; }

    public long getClasses() { return classes; }
    public void setClasses(long classes) { this.classes = classes; }

    public long getRejetes() { return rejetes; }
    public void setRejetes(long rejetes) { this.rejetes = rejetes; }

    public long getUrgents() { return urgents; }
    public void setUrgents(long urgents) { this.urgents = urgents; }
}