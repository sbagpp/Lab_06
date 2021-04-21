package it.polito.tdp.meteo.model;

import java.util.ArrayList;
import java.util.List;

import it.polito.tdp.meteo.DAO.MeteoDAO;

public class Model {
	
	private final static int COST = 100;
	private final static int NUMERO_GIORNI_CITTA_CONSECUTIVI_MIN = 3;
	private final static int NUMERO_GIORNI_CITTA_MAX = 6;
	private final static int NUMERO_GIORNI_TOTALI = 15;
	private MeteoDAO meteoDAO;
	private List<Citta> city;
	private Double costoBest;
	private List<Citta> sequenzaBest;
	private int contaGiorniConsequtivi;
	
	public Model() {
		this.meteoDAO = new MeteoDAO();
		this.city = new ArrayList<>();
		this.city.add(new Citta("Milano"));
		this.city.add(new Citta("Torino"));
		this.city.add(new Citta("Genova"));
		this.sequenzaBest = new ArrayList<>();
		this.contaGiorniConsequtivi = 0;
		this.costoBest = 0.0;
		
	}

	// of course you can change the String output with what you think works best
	public Double getUmiditaMedia(int mese, String localita) {
		Double media = 0.0;
		List <Rilevamento> list = this.meteoDAO.getAllRilevamentiLocalitaMese(mese, localita);
		for(Rilevamento r : list)
			media+=r.getUmidita();
		media=media/(double)list.size();
		return media;
	}
	
	// of course you can change the String output with what you think works best
	public List<Citta> trovaSequenza(int mese) {
		List <Citta> parziale = new ArrayList<>();
		
		for(Citta c : this.city) {
			c.setRilevamenti(this.meteoDAO.get15RilevamentiLocalitaMese(mese, c.getNome()));
		}
		cerca(0, parziale);
		return this.sequenzaBest;
	}

	private void cerca(int livello, List<Citta> parziale) {
		//caso termianale, quando la lunghezza di parziale é pari a 15
		//System.out.println(parziale+"\n"+livello);
		if(parziale.size() == this.NUMERO_GIORNI_TOTALI) {
			Double costoP = this.calcolaCosto(parziale);
			this.contaGiorniConsequtivi = 0; // inizio a fare una nuova soluzione, quindi nessuna cittá ha ancora fatto gg consequitivi
			if(this.costoBest == 0 || this.costoBest >= costoP) {// se la condizione é valida, vuoldire che che parziale é una soluzione interesante
				this.costoBest = costoP;
				this.sequenzaBest = new ArrayList<>(parziale);				
			}
		}
		
		for(Citta c : this.city) {
			if(cityIsValid(c, parziale)) {
				parziale.add(c);
				this.cerca(livello+1, parziale);
				parziale.remove(c);
			}
		}
		
	}

	private boolean cityIsValid(Citta c, List<Citta> parziale) {
		// TODO Auto-generated method stub
		if(parziale.size() == 0) {//é la prima cittá che inserisco quindi posso accetarla a priori
			this.contaGiorniConsequtivi++;
			return true;
		}
	
		int numeroDiVolteInCuiLaCittaCompareInPArziale = 0;
		for(Citta ci : parziale) {
			if(ci.equals(c)) {
				numeroDiVolteInCuiLaCittaCompareInPArziale++;
			}
		}
		if(numeroDiVolteInCuiLaCittaCompareInPArziale >= this.NUMERO_GIORNI_CITTA_MAX) {//la citta é presente troppe volte
			return false;
		}
		
		if(c.equals(parziale.get(parziale.size()-1))) { // il giorno prima  il ricercatore era pressente nella cittá, poihé la cittá non é stata insserita troppe volte, esso puó rimanere nella stessa cittá
			this.contaGiorniConsequtivi++;
			return true;
		}
		
		if(this.contaGiorniConsequtivi >= this.NUMERO_GIORNI_CITTA_CONSECUTIVI_MIN
				&&
				c.equals(parziale.get(parziale.size()-1)) == false) { // il ircercatore h apassato almeno 3 gg consequtivi nella citta precedente e ora puó spostarsi tranquillamente.
			this.contaGiorniConsequtivi = 1;//
			return true;
		}
		
		return false; 
	}

	private Double calcolaCosto(List<Citta> parziale) {
		Double c = 0.0;
		for(int i = 0; i < parziale.size() ;  i++) {
			c+=parziale.get(i).getRilevamenti().get(i).getUmidita(); //ogni gg sommo l'umiditá
			if(i !=0
					&&
					!parziale.get(i).equals(parziale.get(i-1))) {// il primo spostamento e gratis, dopo di che chiedo se la citta del gg i é diversa dal quella nell gg+1 -->  se sono diverse vuol dir eche ho uno spostamento
				c+=this.COST;
			}
		}
		return c;
	}
	

}
