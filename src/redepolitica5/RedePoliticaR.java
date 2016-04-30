package redepolitica5;

import redepolitica4.*;
import irational.IRational;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

/**
 *
 * @author jesjf
 */
public class RedePoliticaR implements IRedePolitica
{
    IRational votosValidosOriginais;
    IRational votosValidosCorrentes;
    HashMap<Integer,Candidato> candidatos;
    HashMap<Integer,Candidato> remanescentes;
    HashMap<Integer,Candidato> eleitos;
    HashMap<Integer,Candidato> eliminados;
    IRational quocienteEleitoral;
    IRational numeroDeCadeiras;
    IRational numeroDeCandidatosEleitosSemVizinhos;
    IRational votosEmCandidatosEleitosSemVizinhos;
    IRational.Factory numberFactory;
    public String toString()
    {
        StringBuffer sb = new StringBuffer();
        for (Candidato c:candidatos.values())        
            sb.append(c.toStringWithLinks());
        return sb.toString();
    }        
    @Override
    public boolean equals(Object o)
    {
        RedePoliticaR rp = (RedePoliticaR)o;
        if (rp.candidatos.size()!=candidatos.size())
            return false;
        for (Candidato c:candidatos.values())
        {
            Candidato oc = rp.getCandidato(c.numero);
            if (oc==null)
                return false;
            if (!c.equals(oc))
                return false;
        }    
        return true;
    }        
    
    public HashMap<Integer, Candidato> getEleitos()
    {
        return eleitos;
    }
    private static boolean eqd(double d1,double d2)
    {
        return (Math.abs(d1-d2) < 1e-5);            
    }            
    public RedePoliticaR(IRational.Factory numberFactory)
    {
        this.numberFactory = numberFactory;
        remanescentes = new HashMap();
        eleitos = new HashMap();
        eliminados = new HashMap();        
        votosValidosOriginais = numberFactory.valueOf(0,1);
        candidatos =  new HashMap();
        numTh = Runtime.getRuntime().availableProcessors();
        for (int t=0; t<numTh; t++)
            new Thread(new Remover(t)).start();        
    }        
    public Candidato getCandidato(int numero)
    {
        return candidatos.get(numero);
    }        
    public void addCandidato(int numero)
    {
         candidatos.put(numero, new Candidato(numero,false));    
    }        
    public void addBandeira(int numero)
    {
         candidatos.put(numero, new Candidato(numero,true));    
    }            
    public void addRelacao(int numeroCandidatoOrigem,int numeroCandidatoVizinho,IRational percentual)
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
        candidatos.get(numero).defineVotos(numberFactory.valueOf(votosProprios,1));
    }        
    public void defineCadeiras(int numeroDeCadeiras)
    {
        this.numeroDeCadeiras = numberFactory.valueOf(numeroDeCadeiras, 1);
    }        
    
    void checa()
    {
        IRational qnc = quocienteEleitoral.times(numeroDeCadeiras);
        if (!votosValidosCorrentes.equals(qnc))
            throw new RuntimeException("Inconsistência no coeficiente eleitoral");
        IRational tNumeroDeCandidatosEleitosSemVizinhos = numberFactory.valueOf(0,1);
        IRational  tVotosEmCandidatosEleitosSemVizinhos = numberFactory.valueOf(0,1);
        IRational  tVotosValidosCorrentes = numberFactory.valueOf(0,1);        
        for (Candidato c: candidatos.values())
        {    
            c.checa(quocienteEleitoral,numberFactory);
            if (c.vizinhosCorrentes!=null && c.vizinhosCorrentes.isEmpty() && c.status == Candidato.ST_ELEITO)
            {    
                tNumeroDeCandidatosEleitosSemVizinhos = tNumeroDeCandidatosEleitosSemVizinhos.plus(numberFactory.valueOf(1,1));
                tVotosEmCandidatosEleitosSemVizinhos = tVotosEmCandidatosEleitosSemVizinhos.plus(c.votosCorrentes);
            }    
            tVotosValidosCorrentes = tVotosValidosCorrentes.plus(c.votosCorrentes);
        }
        if (!tVotosValidosCorrentes.equals(votosValidosCorrentes))
            throw new RuntimeException("Inconsistência no total de votos válidos");
        if (!tNumeroDeCandidatosEleitosSemVizinhos.equals(numeroDeCandidatosEleitosSemVizinhos))
            throw new RuntimeException("Inconsistência numeroDeCandidatosEleitosSemVizinhos");
        if (!tVotosEmCandidatosEleitosSemVizinhos.equals(votosEmCandidatosEleitosSemVizinhos))
            throw new RuntimeException("Inconsistência votosEmCandidatosEleitosSemVizinhos");
    }        
    
    public void preparaCandidatos()
    {
        for (Candidato c:candidatos.values())
            c.preparaParaInicioDaApuracao();
    }        
    
    public void realizaApuracao()
    {
        votosValidosOriginais = numberFactory.valueOf(0,1);
        numeroDeCandidatosEleitosSemVizinhos = numberFactory.valueOf(0,1);
        votosEmCandidatosEleitosSemVizinhos = numberFactory.valueOf(0,1);        
        preparaCandidatos();
        for (Candidato c:candidatos.values())
            votosValidosOriginais = votosValidosOriginais.plus(c.votosProprios);   
        remanescentes = new HashMap(candidatos);
        eleitos = new HashMap();
        eliminados = new HashMap();
        votosValidosCorrentes = votosValidosOriginais;
        quocienteEleitoral =  votosValidosCorrentes.divide(numeroDeCadeiras);
        
        eliminaBandeirasEAtualizaCoeficiente();
        identificaEleitosRepassaVotosEAtualizaCoeficiente();
        checa();
        System.out.println("eleitos: "+eleitos.size()+" eliminados: "+eliminados.size()+" remanescentes: "+remanescentes.size());
        while (!remanescentes.isEmpty())
        {
            checa();
            eliminaUltimoRepassaVotosEAtualizaCoeficiente();
            identificaEleitosRepassaVotosEAtualizaCoeficiente();
            System.out.println("eleitos: "+eleitos.size()+" eliminados: "+eliminados.size()+" remanescentes: "+remanescentes.size()+" votosValidos "+votosValidosCorrentes);
        }            
    }        
    HashMap<Integer,Candidato> identificaRecemEleitos()
    {
        boolean last = remanescentes.size() == 1; // para compensar erros de arredondamento
        HashMap<Integer,Candidato> recemEleitos = new HashMap();
        for (Iterator<Candidato> i=remanescentes.values().iterator();i.hasNext(); )
        {
            Candidato c = i.next();
            if (c.votosCorrentes.compareTo(quocienteEleitoral) >= 0 || last)
            {    
                recemEleitos.put(c.numero,c);
                eleitos.put(c.numero, c);
                c.status = Candidato.ST_ELEITO;
                c.votosNaDefinicao = c.votosCorrentes;
                i.remove();
                if (c.vizinhosCorrentes.isEmpty())
                {    
                   numeroDeCandidatosEleitosSemVizinhos = numeroDeCandidatosEleitosSemVizinhos.plus(numberFactory.valueOf(1,1));
                   votosEmCandidatosEleitosSemVizinhos = votosEmCandidatosEleitosSemVizinhos.plus(c.votosCorrentes);
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

    private void eliminaEAtualizaCoeficiente(Candidato eliminado)
    {
        HashMap<Integer,Candidato> recemEliminado = new HashMap();
        recemEliminado.put(eliminado.numero, eliminado);
        eliminados.put(eliminado.numero, eliminado);
        eliminado.status = Candidato.ST_ELIMINADO;        
        eliminado.votosNaDefinicao = eliminado.votosCorrentes;
        remanescentes.remove(eliminado.numero);
        removeIncrementalmenteDosConjuntosDeVizinhos(recemEliminado);
        System.out.println("eliminado votos "+eliminado.votosCorrentes+"   coeficiente "+quocienteEleitoral);
        repassaPelaRedeEAtualizaCoeficiente(eliminado);                    
        eliminado.vizinhosCorrentes = null;
        
    }        

    private void eliminaBandeirasEAtualizaCoeficiente()
    {
        ArrayList<Candidato> cands = new ArrayList(remanescentes.values()); 
        for (Candidato c:cands)        
            if (c.isBandeira)
                eliminaEAtualizaCoeficiente(c);                
    }        
    
    private void eliminaUltimoRepassaVotosEAtualizaCoeficiente()
    {
        Candidato eliminado = null;
        for (Candidato c:remanescentes.values())        
        {
            if (eliminado == null || c.votosCorrentes.compareTo(eliminado.votosCorrentes) < 0)
                eliminado = c;
        }    
        eliminaEAtualizaCoeficiente(eliminado);                
    }        
    
    private void removeIncrementalmenteDosConjuntosDeVizinhos(HashMap<Integer,Candidato> recemDefinidos)
    {
        for (Candidato c:recemDefinidos.values())
              removeDosConjuntosDeVizinhos(c);
    }        
    
    public void removeDosConjuntosDeVizinhos(int num)
    {
        Candidato c= candidatos.get(num);
        removeDosConjuntosDeVizinhos(c);
    }        
    private void removeDosConjuntosDeVizinhosST(Candidato c)
    {
        HashSet<Candidato> contraVizinhos = new HashSet(c.contraVizinhosCorrentes.values());

        for (Candidato contraVizinho:contraVizinhos)
            if (contraVizinho.status!=Candidato.ST_ELIMINADO) 
                removeDoConjuntoDeVizinhosSemAjustarContravizinhos(c,contraVizinho);
            else
                throw new RuntimeException("Eliminado em conjunto de contravizinhos");
        
        for (Relacao r:c.vizinhosCorrentes.values())
        {
            Candidato vizinho = r.relacionado;
            if (vizinho.status!=Candidato.ST_ELIMINADO) 
                ajustaContraVizinhos(c,vizinho);
            else
                throw new RuntimeException("Eliminado em conjunto de vizinhos");
        }        
        c.contraVizinhosCorrentes = null;                
    }        

    private void removeDosConjuntosDeVizinhos(Candidato c)
    {
        HashSet<Candidato> contraVizinhos = new HashSet(c.contraVizinhosCorrentes.values());

        removeEntries = new ArrayList[numTh];
        for (int t=0; t<numTh; t++)
           removeEntries[t] = new ArrayList();
            
        int  p = 0;
        for (Candidato contraVizinho:contraVizinhos)
        {    
            if (contraVizinho.status!=Candidato.ST_ELIMINADO) 
            {    
                removeEntries[p].add(new RemoveEntry(c,contraVizinho));                
                p = (p+1) % numTh;
            }    
            else
                throw new RuntimeException("Eliminado em conjunto de contravizinhos");
        }            
        synchronized(this)
        {
            removeState = RS_VIZINHOS;
            numDone = 0;
            notifyAll();
            try { wait();} catch (InterruptedException ex) {throw new RuntimeException(ex); }
        }    
        
        removeEntries = new ArrayList[numTh];
        for (int t=0; t<numTh; t++)
           removeEntries[t] = new ArrayList();
        
        p = 0;
        for (Relacao r:c.vizinhosCorrentes.values())
        {
            Candidato vizinho = r.relacionado;
            if (vizinho.status!=Candidato.ST_ELIMINADO) 
            {    
                removeEntries[p].add(new RemoveEntry(c,vizinho));                
                p = (p+1) % numTh;
            }    
            else
                throw new RuntimeException("Eliminado em conjunto de vizinhos");
        }        
        synchronized(this)
        {
            removeState = RS_CONTRAVIZINHOS;
            numDone = 0;
            notifyAll();
            try { wait();} catch (InterruptedException ex) {throw new RuntimeException(ex); }
        }            
        c.contraVizinhosCorrentes = null;                
    }        
    
    int numTh;
    int numDone;
    int removeState;
    static final int RS_WAIT = 0;
    static final int RS_VIZINHOS = 1;
    static final int RS_CONTRAVIZINHOS = 2;
    static final int RS_DONE = 3;
    static class RemoveEntry
    {        
        Candidato candidatoASerRemovido;
        Candidato candidatoDeOndeRemover;
        public RemoveEntry(Candidato candidatoASerRemovido, Candidato candidatoDeOndeRemover)
        {
            this.candidatoASerRemovido = candidatoASerRemovido;
            this.candidatoDeOndeRemover = candidatoDeOndeRemover;
        }        
    }
    ArrayList<RemoveEntry>[] removeEntries;
    
    class Remover implements Runnable
    {
        int num;
        public Remover(int num)
        {
            this.num = num;
        }        
        @Override
        public void run()
        {
           doRemoves(num);
        }        
    }
    
    private void doRemoves(int tn)
    {        
        while (true)
        {
            int act;
            synchronized(this)
            {
                while (removeState==RS_WAIT)
                    try { wait();} catch (InterruptedException ex) {throw new RuntimeException(ex); }
                act = removeState;
            }    
            if (act==RS_DONE)
                break;
            if (act==RS_VIZINHOS)
            {
                for (RemoveEntry re:removeEntries[tn])
                    removeDoConjuntoDeVizinhosSemAjustarContravizinhos(re.candidatoASerRemovido,re.candidatoDeOndeRemover);
            }    
            if (act==RS_CONTRAVIZINHOS)
            {
                for (RemoveEntry re:removeEntries[tn])
                    ajustaContraVizinhos(re.candidatoASerRemovido,re.candidatoDeOndeRemover);
            }                            
            synchronized(this)
            {
                numDone++;
                if (numDone==numTh)
                {
                    removeState=RS_WAIT;
                    notifyAll();
                }    
                else
                    try { wait();} catch (InterruptedException ex) {throw new RuntimeException(ex); }
            }
        }    
    }        

    private void removeDoConjuntoDeVizinhosSemAjustarContravizinhos(Candidato candidatoASerRemovido,Candidato candidatoDeOndeRemover)
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
               synchronized(this)
               {    
                    numeroDeCandidatosEleitosSemVizinhos = numeroDeCandidatosEleitosSemVizinhos.plus(numberFactory.valueOf(1,1));
                    votosEmCandidatosEleitosSemVizinhos = votosEmCandidatosEleitosSemVizinhos.plus(candidatoDeOndeRemover.votosCorrentes);
               }     
            }            
            candidatoDeOndeRemover.vizinhosCorrentes.remove(candidatoASerRemovido.numero);            
        }   
        else
        {
            IRational percentualIda = candidatoDeOndeRemover.vizinhosCorrentes.get(candidatoASerRemovido.numero).percentualRepasse;
            IRational percentualVolta = numberFactory.valueOf(0,1);            
            if (candidatoASerRemovido.vizinhosCorrentes.containsKey(candidatoDeOndeRemover.numero))
                   percentualVolta = candidatoASerRemovido.vizinhosCorrentes.get(candidatoDeOndeRemover.numero).percentualRepasse;
            
            IRational percentualIdaEVolta = percentualIda.times(percentualVolta);
            IRational percentualRestandeDeIdaEVolta = numberFactory.valueOf(1,1).minus(percentualIdaEVolta);
            
            candidatoDeOndeRemover.vizinhosCorrentes.remove(candidatoASerRemovido.numero);
            if (candidatoDeOndeRemover.vizinhosCorrentes.isEmpty() && candidatoDeOndeRemover.status==Candidato.ST_ELEITO)
            {
               synchronized(this)
               {                    
                    numeroDeCandidatosEleitosSemVizinhos = numeroDeCandidatosEleitosSemVizinhos.plus(numberFactory.valueOf(1,1));
                    votosEmCandidatosEleitosSemVizinhos = votosEmCandidatosEleitosSemVizinhos.plus(candidatoDeOndeRemover.votosCorrentes);
               }     
            }    
                            
            for (Relacao r:candidatoDeOndeRemover.vizinhosCorrentes.values())
                r.percentualRepasse = r.percentualRepasse.divide(percentualRestandeDeIdaEVolta);   
            
            for (Relacao r:candidatoASerRemovido.vizinhosCorrentes.values())
            {
                if (r.relacionado==candidatoDeOndeRemover)
                    continue;
                Relacao r2 = candidatoDeOndeRemover.vizinhosCorrentes.get(r.relacionado.numero);
                if (r2==null)
                {    
                    r2 = new Relacao(r.relacionado,numberFactory.valueOf(0,1));
                    candidatoDeOndeRemover.vizinhosCorrentes.put(r2.relacionado.numero, r2);   
                }    
                r2.percentualRepasse = r2.percentualRepasse.plus(percentualIda.times(r.percentualRepasse.divide(percentualRestandeDeIdaEVolta)));
            }            
        }            
        //checa();
    }        
    
    private void ajustaContraVizinhos(Candidato candidatoASerRemovido,Candidato candidatoDeOndeRemover)
    {
        if (!candidatoDeOndeRemover.contraVizinhosCorrentes.containsKey(candidatoASerRemovido.numero))
            throw new RuntimeException("Inconsistência nos conjuntos de contravizinhos");

        
        for (Candidato contraVizinho:candidatoASerRemovido.contraVizinhosCorrentes.values())            
           if (contraVizinho != candidatoDeOndeRemover)           
               candidatoDeOndeRemover.contraVizinhosCorrentes.put(contraVizinho.numero, contraVizinho);

        if (candidatoASerRemovido.status==Candidato.ST_ELIMINADO)
           candidatoDeOndeRemover.contraVizinhosCorrentes.remove(candidatoASerRemovido.numero);
    }        
    
    private void repassaPelaRedeEAtualizaCoeficiente(Candidato recemEliminado)
    {
        
        IRational votosEmCandidatosQueDescartam = votosEmCandidatosEleitosSemVizinhos;
        boolean eliminadoDescarta = recemEliminado!=null && recemEliminado.vizinhosCorrentes.isEmpty();
        if (eliminadoDescarta)
            votosEmCandidatosQueDescartam = votosEmCandidatosQueDescartam.plus(recemEliminado.votosCorrentes);
        
        HashSet<Candidato> candidatosComVotosARepassar = new HashSet();
        if (recemEliminado!=null)
            candidatosComVotosARepassar.add(recemEliminado);
        
        
        if (!numeroDeCandidatosEleitosSemVizinhos.isZero() || eliminadoDescarta)
        {       
            if (numeroDeCandidatosEleitosSemVizinhos.equals(numeroDeCadeiras))
               quocienteEleitoral = numberFactory.valueOf(0,1);
            else    
               quocienteEleitoral = votosValidosCorrentes.minus(votosEmCandidatosQueDescartam).divide(numeroDeCadeiras.minus(numeroDeCandidatosEleitosSemVizinhos));            
            System.out.println("quocienteEleitoral reduzido para "+quocienteEleitoral);
        }  
        for (Candidato c:eleitos.values())
           if (c.votosCorrentes.compareTo(quocienteEleitoral)>=0) 
              candidatosComVotosARepassar.add(c);
        
        for (Candidato c:candidatosComVotosARepassar) 
        {
            IRational votosRepassaveis =  (c==recemEliminado) ? c.votosCorrentes : c.votosCorrentes.minus(quocienteEleitoral);
            if (c.vizinhosCorrentes.isEmpty())
                votosValidosCorrentes = votosValidosCorrentes.minus(votosRepassaveis);
            else
                for (Relacao r:c.vizinhosCorrentes.values())
                    r.relacionado.votosCorrentes = r.relacionado.votosCorrentes.plus(votosRepassaveis.times(r.percentualRepasse));
            c.votosCorrentes = c.votosCorrentes.minus(votosRepassaveis);
        }    
        votosEmCandidatosEleitosSemVizinhos = quocienteEleitoral.times(numeroDeCandidatosEleitosSemVizinhos);
    }        
    
    public static void main(String[] args)
    {
    }
}
