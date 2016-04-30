package redepolitica2;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

/**
 *
 * @author jesjf
 */
public class RedePolitica
{
    class Relacao
    {
        Candidato relacionado;
        double percentualRepasse;
        public Relacao(Candidato relecionado, double percentualRepasse)
        {
            this.relacionado = relecionado;
            this.percentualRepasse = percentualRepasse;
        }                
    }
    class Candidato
    {
        int numero;
        public static final int ST_REMANESCENTE = 0;
        public static final int ST_ELEITO = 1;
        public static final int ST_ELIMINADO = 2;
        int status;
        double votosProprios;
        double votosCorrentes;
        HashMap<Integer,Relacao> vizinhosOriginais;        
        HashMap<Integer,Relacao> vizinhosCorrentes;        
        HashMap<Integer,Candidato> contraVizinhosOriginais;                
        HashMap<Integer,Candidato> contraVizinhosCorrentes;        
        
        public String toString()
        {
            return "numero "+numero+" status "+status+" votos "+votosCorrentes+" "+" v "+vizinhosCorrentes.size()+" cv "+contraVizinhosCorrentes.size();
        }        

        public String toStringWithLinks()
        {
            StringBuffer sb = new StringBuffer();
            sb.append(""+numero+"\n");
            for (Relacao r:vizinhosCorrentes.values())
            {
                sb.append("    "+r.relacionado.numero+"       "+r.percentualRepasse+"\n");
            }                
            return sb.toString();
        }   
        
        
        Candidato(int numero)
        {
            this.numero =numero;            
            vizinhosOriginais = new HashMap();
            contraVizinhosOriginais = new HashMap();
        }        
        void defineVotos(int votosProprios)
        {
            this.votosProprios = votosProprios;
            votosCorrentes = votosProprios;            
        }        
        void adicionaVizinho(Candidato vizinho,double percentual)
        {
            vizinhosOriginais.put(vizinho.numero,new Relacao(vizinho,percentual));
        }        
        void adicionaContraVizinho(Candidato contraVizinho)
        {
            contraVizinhosOriginais.put(contraVizinho.numero,contraVizinho);
        }                
        void preparaParaInicioDaApuracao()
        {
            status = ST_REMANESCENTE;
            vizinhosCorrentes = new HashMap(vizinhosOriginais);
            contraVizinhosCorrentes = new HashMap(contraVizinhosOriginais);
        }        

        private void checa()
        {
            if (status == ST_ELEITO && !eqd(votosCorrentes,quocienteEleitoral))
                throw new RuntimeException("Eleito com votos diferentes do coeficiente");
            else
            if (status == ST_ELIMINADO && votosCorrentes!=0)
                throw new RuntimeException("Eliminado manteve votos");
            else
            if (status == ST_REMANESCENTE &&  votosCorrentes >= quocienteEleitoral)    
                throw new RuntimeException("Remanescente deveria ter sido eleito");
            double sum = 0;
            for (Relacao r:vizinhosCorrentes.values())
            {
                sum += r.percentualRepasse;
                if (r.relacionado==this)
                    throw new RuntimeException("Auto relação");
                if (!r.relacionado.contraVizinhosCorrentes.containsKey(numero))
                    throw new RuntimeException("Inconsistência entre vizinhos e contra vizinhos");                
            }    
            for (Candidato c:contraVizinhosCorrentes.values())
                if (!c.vizinhosCorrentes.containsKey(numero))
                     throw new RuntimeException("Inconsistência entre vizinhos e contra vizinhos");                
            
            if (!vizinhosCorrentes.isEmpty() && !eqd(sum,1))
                throw new RuntimeException("Percentuais de repasse inconsistentes");
        }
        
    }

    double votosValidosOriginais;
    double votosValidosCorrentes;
    HashMap<Integer,Candidato> candidatos;
    HashMap<Integer,Candidato> remanescentes;
    HashMap<Integer,Candidato> eleitos;
    HashMap<Integer,Candidato> eliminados;
    double quocienteEleitoral;
    int numeroDeCadeiras;
    int numeroDeCandidatosEleitosSemVizinhos;
    double votosEmCandidatosEleitosSemVizinhos;

    public HashMap<Integer, Candidato> getEleitos()
    {
        return eleitos;
    }

    
    
    
    private static boolean eqd(double d1,double d2)
    {
        return (Math.abs(d1-d2) < 1e-5);            
    }        
    
    public String toString()
    {
        StringBuffer sb = new StringBuffer();
        for (Candidato c:candidatos.values())        
            sb.append(c.toStringWithLinks());
        return sb.toString();
    }        
    
    public RedePolitica()
    {
        remanescentes = new HashMap();
        eleitos = new HashMap();
        eliminados = new HashMap();        
        votosValidosOriginais = 0;
        candidatos =  new HashMap();
    }        
    public void addCandidato(int numero)
    {
         candidatos.put(numero, new Candidato(numero));    
    }        
    public void addRelacao(int numeroCandidatoOrigem,int numeroCandidatoVizinho,double percentual)
    {
        if (numeroCandidatoOrigem==numeroCandidatoVizinho)
            throw new RuntimeException("Auto Relação");
        Candidato origem = candidatos.get(numeroCandidatoOrigem);
        Candidato vizinho = candidatos.get(numeroCandidatoVizinho);
        if (origem ==null)
            throw new RuntimeException("Candidato Inexistente "+numeroCandidatoOrigem);
        if (vizinho==null)
            throw new RuntimeException("Candidato Inexistente "+numeroCandidatoVizinho);
        
         origem.adicionaVizinho(vizinho,percentual);      
         vizinho.adicionaContraVizinho(origem);
    }        
    public void defineVotos(int numero,int votosProprios)
    {
        if (!candidatos.containsKey(numero))
            throw new RuntimeException("Candidato Inexistente "+numero);
        candidatos.get(numero).defineVotos(votosProprios);
    }        
    public void defineCadeiras(int numeroDeCadeiras)
    {
        this.numeroDeCadeiras = numeroDeCadeiras;
    }        
    
    void checa()
    {
        double qnc = quocienteEleitoral * numeroDeCadeiras;
        if (!eqd(votosValidosCorrentes,qnc))
            throw new RuntimeException("Inconsistência no coeficiente eleitoral");
        int tNumeroDeCandidatosEleitosSemVizinhos = 0;
        double tVotosEmCandidatosEleitosSemVizinhos = 0;
        double tVotosValidosCorrentes = 0;        
        for (Candidato c: candidatos.values())
        {    
            c.checa();
            if (c.vizinhosCorrentes.isEmpty() && c.status == Candidato.ST_ELEITO)
            {    
                tNumeroDeCandidatosEleitosSemVizinhos++;
                tVotosEmCandidatosEleitosSemVizinhos += c.votosCorrentes;
            }    
            tVotosValidosCorrentes += c.votosCorrentes;
        }
        if (!eqd(tVotosValidosCorrentes,votosValidosCorrentes))
            throw new RuntimeException("Inconsistência no total de votos válidos");
        if (tNumeroDeCandidatosEleitosSemVizinhos != numeroDeCandidatosEleitosSemVizinhos)
            throw new RuntimeException("Inconsistência numeroDeCandidatosEleitosSemVizinhos");
        if (!eqd(tVotosEmCandidatosEleitosSemVizinhos,votosEmCandidatosEleitosSemVizinhos))
            throw new RuntimeException("Inconsistência votosEmCandidatosEleitosSemVizinhos");
    }        
    
    public void realizaApuracao()
    {
        votosValidosOriginais = 0;
        numeroDeCandidatosEleitosSemVizinhos = 0;
        votosEmCandidatosEleitosSemVizinhos = 0;        
        for (Candidato c:candidatos.values())
        {    
            c.preparaParaInicioDaApuracao();
            votosValidosOriginais += c.votosProprios;   
        }    
        remanescentes = new HashMap(candidatos);
        eleitos = new HashMap();
        eliminados = new HashMap();
        votosValidosCorrentes = votosValidosOriginais;
        quocienteEleitoral =  votosValidosCorrentes / numeroDeCadeiras;
        
        //checa();
        
        identificaEleitosRepassaVotosEAtualizaCoeficiente();
        System.out.println("eleitos: "+eleitos.size()+" eliminados: "+eliminados.size()+" remanescentes: "+remanescentes.size());
        while (!remanescentes.isEmpty())
        {
            checa();
            eliminaUltimoRepassaVotosEAtualizaCoeficiente();
            identificaEleitosRepassaVotosEAtualizaCoeficiente();
            System.out.println("eleitos: "+eleitos.size()+" eliminados: "+eliminados.size()+" remanescentes: "+remanescentes.size()+" votosValidos "+votosValidosCorrentes+" Proto = "+candidatos.get(6588).votosCorrentes);
        }            
    }        
    HashMap<Integer,Candidato> identificaRecemEleitos()
    {
        HashMap<Integer,Candidato> recemEleitos = new HashMap();
        for (Iterator<Candidato> i=remanescentes.values().iterator();i.hasNext(); )
        {
            Candidato c = i.next();
            if (c.votosCorrentes >= quocienteEleitoral)
            {    
                recemEleitos.put(c.numero,c);
                eleitos.put(c.numero, c);
                c.status = Candidato.ST_ELEITO;
                i.remove();
                if (c.vizinhosCorrentes.isEmpty())
                {    
                   numeroDeCandidatosEleitosSemVizinhos++;
                   votosEmCandidatosEleitosSemVizinhos += c.votosCorrentes;
                }   
            }    
        }    
        return recemEleitos;        
    }        
            
    
    private void identificaEleitosRepassaVotosEAtualizaCoeficiente()
    {
        HashMap<Integer,Candidato> recemEleitos = identificaRecemEleitos();
        while (!recemEleitos.isEmpty())
        {            
            removeIncrementalmenteDosConjuntosDeVizinhos(recemEleitos);
            repassaPelaRedeEAtualizaCoeficiente(null);            
            recemEleitos = identificaRecemEleitos();
        }    
    }        
    
    private void eliminaUltimoRepassaVotosEAtualizaCoeficiente()
    {
        Candidato eliminado = null;
        for (Candidato c:remanescentes.values())        
        {
            if (eliminado == null || c.votosCorrentes < eliminado.votosCorrentes)
                eliminado = c;
        }    
        HashMap<Integer,Candidato> recemEliminado = new HashMap();
        recemEliminado.put(eliminado.numero, eliminado);
        eliminados.put(eliminado.numero, eliminado);
        eliminado.status = Candidato.ST_ELIMINADO;        
        remanescentes.remove(eliminado.numero);
        removeIncrementalmenteDosConjuntosDeVizinhos(recemEliminado);
        System.out.println("eliminado votos "+eliminado.votosCorrentes+"   coeficiente "+quocienteEleitoral);
        repassaPelaRedeEAtualizaCoeficiente(eliminado);                    
        
    }        
    
    private void removeIncrementalmenteDosConjuntosDeVizinhos(HashMap<Integer,Candidato> recemDefinidos)
    {
        for (Candidato c:recemDefinidos.values())
              removeDosConjuntosDeVizinhos(c);
    }        
    
    private void removeDosConjuntosDeVizinhos(Candidato c)
    {
        HashSet<Candidato> contraVizinhos = new HashSet(c.contraVizinhosCorrentes.values());
        for (Candidato contraVizinho:contraVizinhos)
            	removeDoConjuntoDeVizinhos(c,contraVizinho);
    }        
    
    private void removeDoConjuntoDeVizinhos(Candidato candidatoASerRemovido,Candidato candidatoDeOndeRemover)
    {
        if (!candidatoDeOndeRemover.vizinhosCorrentes.containsKey(candidatoASerRemovido.numero))
            throw new RuntimeException("Inconsistência nos conjuntos de vizinhos");
        if (
               candidatoASerRemovido.vizinhosCorrentes.size()==1 && 
               candidatoASerRemovido.vizinhosCorrentes.containsKey(candidatoDeOndeRemover.numero) &&
               candidatoDeOndeRemover.vizinhosCorrentes.size()==1
            )
        {
            if (candidatoDeOndeRemover.status==Candidato.ST_ELEITO)
            {    
               numeroDeCandidatosEleitosSemVizinhos++;
               votosEmCandidatosEleitosSemVizinhos += candidatoDeOndeRemover.votosCorrentes;
            }            
            candidatoDeOndeRemover.vizinhosCorrentes.remove(candidatoASerRemovido.numero);            
            candidatoASerRemovido.contraVizinhosCorrentes.remove(candidatoDeOndeRemover.numero);
        }   
        else
        {
            double percentualIda = candidatoDeOndeRemover.vizinhosCorrentes.get(candidatoASerRemovido.numero).percentualRepasse;
            double percentualVolta = 0;            
            if (candidatoASerRemovido.vizinhosCorrentes.containsKey(candidatoDeOndeRemover.numero))
                   percentualVolta = candidatoASerRemovido.vizinhosCorrentes.get(candidatoDeOndeRemover.numero).percentualRepasse;
            
            candidatoDeOndeRemover.vizinhosCorrentes.remove(candidatoASerRemovido.numero);
            if (candidatoDeOndeRemover.vizinhosCorrentes.isEmpty() && candidatoDeOndeRemover.status==Candidato.ST_ELEITO)
            {
               numeroDeCandidatosEleitosSemVizinhos++;
               votosEmCandidatosEleitosSemVizinhos += candidatoDeOndeRemover.votosCorrentes;                
            }    
                
            candidatoASerRemovido.contraVizinhosCorrentes.remove(candidatoDeOndeRemover.numero);        
            
            for (Relacao r:candidatoDeOndeRemover.vizinhosCorrentes.values())
                r.percentualRepasse = r.percentualRepasse / (1 - percentualIda * percentualVolta);   
            
            for (Relacao r:candidatoASerRemovido.vizinhosCorrentes.values())
            {
                if (r.relacionado==candidatoDeOndeRemover)
                    continue;
                Relacao r2 = candidatoDeOndeRemover.vizinhosCorrentes.get(r.relacionado.numero);
                if (r2==null)
                {    
                    r2 = new Relacao(r.relacionado,0);
                    candidatoDeOndeRemover.vizinhosCorrentes.put(r2.relacionado.numero, r2);   
                    r2.relacionado.contraVizinhosCorrentes.put(candidatoDeOndeRemover.numero, candidatoDeOndeRemover);
                }    
                r2.percentualRepasse += percentualIda * r.percentualRepasse / (1 - percentualIda * percentualVolta);
            }            
        }            
        //checa();
    }        
    
    private void repassaPelaRedeEAtualizaCoeficiente(Candidato recemEliminado)
    {
        
        double votosEmCandidatosQueDescartam = votosEmCandidatosEleitosSemVizinhos;
        boolean eliminadoDescarta = recemEliminado!=null && recemEliminado.vizinhosCorrentes.isEmpty();
        if (eliminadoDescarta)
            votosEmCandidatosQueDescartam += recemEliminado.votosCorrentes;
        
        HashSet<Candidato> candidatosComVotosARepassar = new HashSet();
        if (recemEliminado!=null)
            candidatosComVotosARepassar.add(recemEliminado);
        
        
        if (numeroDeCandidatosEleitosSemVizinhos!=0 || eliminadoDescarta)
        {                
            quocienteEleitoral = (votosValidosCorrentes - votosEmCandidatosQueDescartam) / (numeroDeCadeiras - numeroDeCandidatosEleitosSemVizinhos);            
            System.out.println("quocienteEleitoral reduzido para "+quocienteEleitoral);
        }  
        for (Candidato c:eleitos.values())
           if (c.votosCorrentes >= quocienteEleitoral) 
              candidatosComVotosARepassar.add(c);
        
        for (Candidato c:candidatosComVotosARepassar) 
        {
            double votosRepassaveis =  (c==recemEliminado) ? c.votosCorrentes : c.votosCorrentes - quocienteEleitoral;
            if (c.vizinhosCorrentes.isEmpty())
                this.votosValidosCorrentes -= votosRepassaveis;
            else
                for (Relacao r:c.vizinhosCorrentes.values())
                    r.relacionado.votosCorrentes += votosRepassaveis * r.percentualRepasse;
            c.votosCorrentes -= votosRepassaveis;
        }    
        votosEmCandidatosEleitosSemVizinhos = quocienteEleitoral * numeroDeCandidatosEleitosSemVizinhos;
    }        
    
    public static void main(String[] args)
    {
    }
}
