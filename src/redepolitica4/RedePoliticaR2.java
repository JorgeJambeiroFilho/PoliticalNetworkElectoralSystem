package redepolitica4;

import gnu.trove.map.hash.THashMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.set.hash.THashSet;
import irational.IRational;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

/**
 *
 * @author jesjf
 */
public class RedePoliticaR2 implements IRedePolitica
{
    IRational votosValidosOriginais;
//    IRational votosValidosCorrentes;
    TIntObjectHashMap<Candidato> candidatos;
    TIntObjectHashMap<Candidato> remanescentes;
    TIntObjectHashMap<Candidato> eleitos;
    TIntObjectHashMap<Candidato> eliminados;
    Candidato descarte;    
    IRational quocienteEleitoral;
    IRational numeroDeCadeiras;
    IRational.Factory numberFactory;
    IRational zero;
    IRational one;
    boolean close;
    public String toString()
    {
        StringBuffer sb = new StringBuffer();
        for (Candidato c:candidatos.valueCollection())        
            sb.append(c.toStringWithLinks());
        sb.append(descarte.toStringWithLinks());
        return sb.toString();
    }        
    @Override
    public boolean equals(Object o)
    {
        RedePoliticaR2 rp = (RedePoliticaR2)o;
        if (rp.candidatos.size()!=candidatos.size())
            return false;
        for (Candidato c:candidatos.valueCollection())
        {
            Candidato oc = rp.getCandidato(c.numero);
            if (oc==null)
                return false;
            if (!c.equals(oc))
                return false;
        }    
        return descarte.equals(rp.descarte);
    }        
    
    public TIntObjectHashMap<Candidato> getEleitos()
    {
        return eleitos;
    }
    public RedePoliticaR2(IRational.Factory numberFactory,String name,int numTh)
    {
        this.numberFactory = numberFactory;
        zero = numberFactory.valueOf(0,1);
        one = numberFactory.valueOf(1,1);
        remanescentes = new TIntObjectHashMap();
        eleitos = new TIntObjectHashMap();
        eliminados = new TIntObjectHashMap();        
        votosValidosOriginais = zero;
        candidatos =  new TIntObjectHashMap();
        descarte = new Candidato(0,false);
        descarte.defineVotos(numberFactory.valueOf(0, 1));
        this.numTh = numTh;//Runtime.getRuntime().availableProcessors();
        for (int t=0; t<numTh; t++)
            new Thread(new Remover(t),"DoRemoves "+name+" "+t).start();        
    }        
    public synchronized void close()
    {
        removeState = RS_DONE;
        notifyAll();
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
    
    public void checa(boolean hasJustElected)
    {
        if (true) return;
        IRational qnc = quocienteEleitoral.times(numeroDeCadeiras);
        IRational votosValidosCorrentes = votosValidosOriginais.minus(descarte.votosCorrentes);
        if (!votosValidosCorrentes.equals(qnc))
            throw new RuntimeException("Inconsistência no coeficiente eleitoral");
        IRational  tVotosValidosCorrentes = zero;        
        for (Candidato c: candidatos.valueCollection())
        {    
            c.checa(quocienteEleitoral,numberFactory,hasJustElected);
            tVotosValidosCorrentes = tVotosValidosCorrentes.plus(c.votosCorrentes);
        }
        if (!tVotosValidosCorrentes.equals(votosValidosCorrentes))
            throw new RuntimeException("Inconsistência no total de votos válidos");
    }        
    
    public void preparaCandidatos()
    {
        for (Candidato c:candidatos.valueCollection())
            c.preparaParaInicioDaApuracao(descarte,numberFactory);
        descarte.preparaParaInicioDaApuracaoDescarte(numberFactory);
    }        
    
    public void preparaComoSeFosseApurar()
    {
        votosValidosOriginais = zero;
        preparaCandidatos();
        for (Candidato c:candidatos.valueCollection())
            votosValidosOriginais = votosValidosOriginais.plus(c.votosProprios);   
        remanescentes = new TIntObjectHashMap(candidatos);
        eleitos = new TIntObjectHashMap();
        eliminados = new TIntObjectHashMap();
        quocienteEleitoral =  votosValidosOriginais.divide(numeroDeCadeiras);        
    }
            
    public void realizaApuracao()
    {
        preparaComoSeFosseApurar();
        
        eliminaBandeirasEAtualizaCoeficiente();
        checa(true);
        identificaEleitosRepassaVotosEAtualizaCoeficiente();
        checa(true);
        System.out.println("eleitos: "+eleitos.size()+" eliminados: "+eliminados.size()+" remanescentes: "+remanescentes.size());
        while (!remanescentes.isEmpty())
        {
            checa(true);
            eliminaUltimoRepassaVotosEAtualizaCoeficiente();
            checa(false);
            identificaEleitosRepassaVotosEAtualizaCoeficiente();
            IRational votosValidosCorrentes = votosValidosOriginais.minus(descarte.getVotosCorrentes());
            System.out.println("eleitos: "+eleitos.size()+" eliminados: "+eliminados.size()+" remanescentes: "+remanescentes.size()+" votosValidos "+votosValidosCorrentes);
        }            
    }        
    HashMap<Integer,Candidato> identificaRecemEleitos()
    {
        boolean last = remanescentes.size() == 1; // para compensar erros de arredondamento
        HashMap<Integer,Candidato> recemEleitos = new HashMap();
        for (Iterator<Candidato> i=remanescentes.valueCollection().iterator();i.hasNext(); )
        {
            Candidato c = i.next();
            if (c.votosCorrentes.compareTo(quocienteEleitoral) >= 0 || last)
            {    
                recemEleitos.put(c.numero,c);
                eleitos.put(c.numero, c);
                c.status = Candidato.ST_ELEITO;
                c.votosNaDefinicao = c.votosCorrentes;
                i.remove();
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
            if (numeroDeCadeiras.equals(numberFactory.valueOf(eleitos.size(),1)))
                break;
            repassaPelaRedeEAtualizaCoeficiente(new ArrayList());            
            recemEleitos = identificaRecemEleitos();
        }    
    }        

    private void eliminaEAtualizaCoeficiente(List<Candidato> listaRecemEliminados)
    {
        HashMap<Integer,Candidato> recemEliminado = new HashMap();
        for (Candidato eliminado:listaRecemEliminados)
        {    
            recemEliminado.put(eliminado.numero, eliminado);
            eliminado.status = Candidato.ST_EM_ELIMINACAO;
        }    
        removeIncrementalmenteDosConjuntosDeVizinhos(recemEliminado);               
        for (Candidato eliminado:listaRecemEliminados)
        {    
            eliminados.put(eliminado.numero, eliminado);
            eliminado.status = Candidato.ST_ELIMINADO;        
            eliminado.votosNaDefinicao = eliminado.votosCorrentes;
            remanescentes.remove(eliminado.numero);
            System.out.println("eliminado votos "+eliminado.votosCorrentes+"   coeficiente "+quocienteEleitoral);
        }                
        repassaPelaRedeEAtualizaCoeficiente(listaRecemEliminados);                    
        for (Candidato eliminado:listaRecemEliminados)
           eliminado.vizinhosCorrentes = null;        
    }        

    private void eliminaBandeirasEAtualizaCoeficiente()
    {
        ArrayList<Candidato> cands = new ArrayList(remanescentes.valueCollection()); 
        ArrayList<Candidato> eliminados = new ArrayList(); 
        for (Candidato c:cands)        
            if (c.isBandeira)
                eliminados.add(c);
        eliminaEAtualizaCoeficiente(eliminados);                
    }        
    
    private void eliminaUltimoRepassaVotosEAtualizaCoeficiente()
    {
        Candidato eliminado = null;
        for (Candidato c:remanescentes.valueCollection())        
        {
            if (eliminado == null || c.votosCorrentes.compareTo(eliminado.votosCorrentes) < 0)
                eliminado = c;
        }    
        ArrayList<Candidato> eliminados = new ArrayList(); 
        eliminados.add(eliminado);
        eliminaEAtualizaCoeficiente(eliminados);                
    }        
    
    private void checaAusencia(Candidato c)
    {
        for (Candidato cc:candidatos.valueCollection())
        {
            if (cc.vizinhosCorrentes!=null && cc.vizinhosCorrentes.containsKey(c.getNumero()))
                throw new RuntimeException("Candidato já removido presente");
        }    
    }        
    
    private void checaContraAusencia(Candidato c)
    {
        for (Candidato cc:candidatos.valueCollection())
            if (cc.contraVizinhosCorrentes!=null && cc.contraVizinhosCorrentes.containsKey(c.getNumero()))
                throw new RuntimeException("Candidato já removido presente (contra)");
    }        

       
    private void removeIncrementalmenteDosConjuntosDeVizinhos(HashMap<Integer,Candidato> recemDefinidos)
    {
        //for (Candidato c:recemDefinidos.values())
        //    c.contraVizinhosTemporarios = new HashMap(c.contraVizinhosCorrentes);
        for (Candidato c:recemDefinidos.values())
        {    
              removeDosConjuntosDeVizinhos(c);
              checaAusencia(c);
        }
//        for (Candidato c:recemDefinidos.values())
//        {    
//            c.contraVizinhosCorrentes = c.contraVizinhosTemporarios;
//        }
    }        
    
    public void removeDosConjuntosDeVizinhos(int num)
    {
        Candidato c= candidatos.get(num);
        removeDosConjuntosDeVizinhos(c);
    }        

    private void removeDosConjuntosDeVizinhos(Candidato c)
    {
        if (c==null)
            throw new RuntimeException("c==null");
        if (c.contraVizinhosCorrentes==null)
            throw new RuntimeException("c.contraVizinhosCorrentes==null");
        THashSet<Candidato> contraVizinhos = new THashSet(c.contraVizinhosCorrentes.valueCollection());

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
        for (Relacao r:c.vizinhosCorrentes.valueCollection())
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
            candidatoDeOndeRemover.vizinhosCorrentes.remove(candidatoASerRemovido.numero);            
            candidatoDeOndeRemover.vizinhosCorrentes.put(descarte.getNumero(), new Relacao(descarte,numberFactory.valueOf(1, 1)));
            //descarte.contraVizinhosCorrentes.put(candidatoDeOndeRemover.numero, candidatoDeOndeRemover);
        }   
        else
        {
            IRational percentualIda = candidatoDeOndeRemover.vizinhosCorrentes.get(candidatoASerRemovido.numero).percentualRepasse;
            IRational percentualVolta = zero;            
            if (candidatoASerRemovido.vizinhosCorrentes.containsKey(candidatoDeOndeRemover.numero))
                   percentualVolta = candidatoASerRemovido.vizinhosCorrentes.get(candidatoDeOndeRemover.numero).percentualRepasse;
            
            IRational percentualIdaEVolta = percentualIda.times(percentualVolta);
            IRational percentualRestandeDeIdaEVolta = one.minus(percentualIdaEVolta);
            
            candidatoDeOndeRemover.vizinhosCorrentes.remove(candidatoASerRemovido.numero);
                            
            for (Relacao r:candidatoDeOndeRemover.vizinhosCorrentes.valueCollection())
                r.percentualRepasse = r.percentualRepasse.divide(percentualRestandeDeIdaEVolta);   
            
            for (Relacao r:candidatoASerRemovido.vizinhosCorrentes.valueCollection())
            {
                if (r.relacionado==candidatoDeOndeRemover)
                    continue;
                Relacao r2 = candidatoDeOndeRemover.vizinhosCorrentes.get(r.relacionado.numero);
                if (r2==null)
                {    
                    r2 = new Relacao(r.relacionado,zero);
                    candidatoDeOndeRemover.vizinhosCorrentes.put(r2.relacionado.numero, r2);   
                }    
                r2.percentualRepasse = r2.percentualRepasse.plus(percentualIda.times(r.percentualRepasse.divide(percentualRestandeDeIdaEVolta)));
            }            
        }            
        //checa();
    }        
    
    private void ajustaContraVizinhos(Candidato candidatoASerRemovido,Candidato candidatoDeOndeRemover)
    {
        if (candidatoDeOndeRemover.status==Candidato.ST_DESCARTE)
            return;
        if (candidatoDeOndeRemover.status==Candidato.ST_ELIMINADO)
            throw new RuntimeException("Ajustando contravizinhos de candidato já eliminado");
        if (candidatoDeOndeRemover.status==Candidato.ST_EM_ELIMINACAO)
            return;
        
       // HashMap<Integer,Candidato> contraVizinhosLocais = //candidatoDeOndeRemover.status==Candidato.ST_EM_ELIMINACAO ? 
                                                          //candidatoDeOndeRemover.contraVizinhosTemporarios :
        //                                                  candidatoDeOndeRemover.contraVizinhosCorrentes;
                
        
        
        if (candidatoDeOndeRemover!=descarte && !candidatoDeOndeRemover.contraVizinhosCorrentes.containsKey(candidatoASerRemovido.numero))
            throw new RuntimeException("Inconsistência nos conjuntos de contravizinhos");

                
        for (Candidato contraVizinho:candidatoASerRemovido.contraVizinhosCorrentes.valueCollection())            
           if (contraVizinho != candidatoDeOndeRemover)           
               candidatoDeOndeRemover.contraVizinhosCorrentes.put(contraVizinho.numero, contraVizinho);

        if (candidatoASerRemovido.status==Candidato.ST_ELIMINADO || candidatoASerRemovido.status==Candidato.ST_EM_ELIMINACAO)
          try
          {    
             candidatoDeOndeRemover.contraVizinhosCorrentes.remove(candidatoASerRemovido.numero);
          }
          catch(RuntimeException e)
          {
             throw e;
          }     
    }        
    
    class DadosTemporariosRepasseEstruturaOriginal
    {
          TIntObjectHashMap<Candidato> candidatos;
          TIntObjectHashMap<Candidato> candidatosFonte;
          IRational quocienteEleitoral;           
          Candidato descarte;
          DadosTemporariosRepasseEstruturaOriginal()
          {              
            quocienteEleitoral = RedePoliticaR2.this.quocienteEleitoral;  
            descarte = new Candidato(RedePoliticaR2.this.descarte);
            candidatos = new TIntObjectHashMap();
            for (Candidato c:RedePoliticaR2.this.candidatos.valueCollection())                
               candidatos.put(c.numero,new Candidato(c));              
            for (Candidato c:candidatos.valueCollection())
            {
                TIntObjectHashMap<Relacao> vizinhosOriginais = new TIntObjectHashMap();
                for (Relacao r:c.vizinhosOriginais.valueCollection())
                    vizinhosOriginais.put(r.relacionado.numero,new Relacao(candidatos.get(r.relacionado.numero),r.percentualRepasse));
                c.vizinhosOriginais = vizinhosOriginais;
            }    
            candidatosFonte = new TIntObjectHashMap();
            for (Candidato c:candidatos.valueCollection())
                if (
                        c.status==Candidato.ST_ELEITO || 
                        c.status==Candidato.ST_ELIMINADO  ||
                        c.status==Candidato.ST_EM_ELIMINACAO 
                   )
                   candidatosFonte.put(c.numero, c); 
          }        
    }
    
    private void repassaPelaRedeEAtualizaCoeficienteEstruturaOriginal(DadosTemporariosRepasseEstruturaOriginal dt)
    {
        int nq = eleitos.isEmpty() ? 1 : 16;
        
        for (int i=0; i<nq; i++)
        {    
/*            
            IRational maxDif = zero;
            IRational totDif =  zero;            
            IRational totMain = zero;
            IRational totCopy = zero;
            for (Candidato c:dt.candidatos.values())
            {
                Candidato cc = candidatos.get(c.numero);
                totMain = totMain.plus(cc.votosCorrentes);
                totCopy = totCopy.plus(c.votosCorrentes);
                IRational dif = c.votosCorrentes.minus(cc.votosCorrentes);
                totDif = totDif.plus(dif);
                if (dif.compareTo(zero) < 0)
                    dif = zero.minus(dif);                
                if (maxDif.compareTo(dif) < 0)                
                    maxDif = dif;
                if (c.status!=Candidato.ST_REMANESCENTE)
                {    
                    IRational vp = c.status==Candidato.ST_ELEITO ? c.votosCorrentes.minus(dt.quocienteEleitoral) : c.votosCorrentes;
                    if (!vp.equals(zero))
                        vp = zero;
                }
            }   
            System.out.println("MAXDIF = "+maxDif.doubleValue()+" desc "+dt.descarte.votosCorrentes +"  "+descarte.votosCorrentes+" TOTDIF = "+totDif);
            System.out.println("TOTMAIN = "+totMain+" TOTCOPY "+totCopy);
*/            
            for (int t=0; t<1024; t++)
                for (Candidato c:dt.candidatosFonte.valueCollection())
                    if (c.status!=Candidato.ST_REMANESCENTE)
                    {
                        IRational vp = c.status==Candidato.ST_ELEITO ? c.votosCorrentes.minus(dt.quocienteEleitoral) : c.votosCorrentes;
                        IRational vp2 = zero;
                        if (!vp.equals(zero))
                        {   
                            if (!c.vizinhosOriginais.isEmpty())
                                c.votosCorrentes = c.votosCorrentes.minus(vp);
                            for (Relacao r:c.vizinhosOriginais.valueCollection())
                            {
                                //if (r.relacionado.numero==28)
                                //    System.out.println("28 "+r.relacionado.votosCorrentes+" "+c.numero+" "+vp.times(r.percentualRepasse));
                                r.relacionado.votosCorrentes = r.relacionado.votosCorrentes.plus(vp.times(r.percentualRepasse));
                                vp2 = vp2.plus(vp.times(r.percentualRepasse));
                            }   
                            if (vp2.minus(vp).doubleValue() > 0.01)
                                System.out.println("Votos brotaram");
                        }    
                    }    
            for (Candidato c:dt.candidatosFonte.valueCollection())
                if (c.status!=Candidato.ST_REMANESCENTE)
                {
                    IRational vp = c.status==Candidato.ST_ELEITO ? c.votosCorrentes.minus(dt.quocienteEleitoral) : c.votosCorrentes;
                    if (!vp.equals(zero))
                    {    
                        c.votosCorrentes = c.votosCorrentes.minus(vp);
                        dt.descarte.votosCorrentes = dt.descarte.votosCorrentes.plus(vp);
                    }    
                }    
            dt.quocienteEleitoral = votosValidosOriginais.minus(dt.descarte.votosCorrentes).divide(numeroDeCadeiras);          
            //if (!dt.descarte.votosCorrentes.isZero())
            //    System.out.println("Descartados em convergência "+dt.descarte.votosCorrentes);
        }           
        IRational maxDif = zero;
        IRational totDif =  zero;            
        IRational totMain = zero;
        IRational totCopy = zero;
        for (Candidato c:dt.candidatos.valueCollection())
        {
            Candidato cc = candidatos.get(c.numero);
            totMain = totMain.plus(cc.votosCorrentes);
            totCopy = totCopy.plus(c.votosCorrentes);
            IRational dif = c.votosCorrentes.minus(cc.votosCorrentes);
            
            if (dif.compareTo(zero) < 0)
                dif = zero.minus(dif);                
            if (maxDif.compareTo(dif) < 0)                
                maxDif = dif;            
            totDif = totDif.plus(dif);
            if (c.status!=Candidato.ST_REMANESCENTE)
            {    
                IRational vp = c.status==Candidato.ST_ELEITO ? c.votosCorrentes.minus(dt.quocienteEleitoral) : c.votosCorrentes;
                if (!vp.equals(zero))
                    vp = zero;
            }
        }   
        System.out.println("MAXDIF = "+maxDif.doubleValue()+" desc "+dt.descarte.votosCorrentes.doubleValue() +"  "+descarte.votosCorrentes.doubleValue()+" TOTDIF = "+totDif.doubleValue());
        System.out.println("TOTMAIN = "+totMain.doubleValue()+" TOTCOPY "+totCopy.doubleValue());
        
    }        
    
    
    static int numRep = 0;
    private void repassaPelaRedeEAtualizaCoeficiente(List<Candidato> recemEliminados)
    {     
        //DadosTemporariosRepasseEstruturaOriginal dt = new DadosTemporariosRepasseEstruturaOriginal();
                
        IRational sumVP = numberFactory.valueOf(0, 1);
        IRational sumP = numberFactory.valueOf(0, 1);        
        for (Candidato c:recemEliminados)
        {    
            IRational v = c.votosCorrentes;
            Relacao rDescarte = c.vizinhosCorrentes.get(descarte.numero);
            IRational p = rDescarte==null ? zero : rDescarte.percentualRepasse;
            IRational vp = v.times(p);
            sumVP = sumVP.plus(vp);
        }
        for (Candidato c:eleitos.valueCollection())
        {    
            IRational v = c.votosCorrentes;
            Relacao rDescarte = c.vizinhosCorrentes.get(descarte.numero);
            IRational p = rDescarte==null ? zero : rDescarte.percentualRepasse;
            IRational vp = v.times(p);
            sumVP = sumVP.plus(vp);
            sumP = sumP.plus(p);
        }
        if (sumP.compareTo(numeroDeCadeiras) >= 0)
            throw new RuntimeException("Percentuais de descarte de eleitos superam cadeiras");
        IRational novoQuocienteEleitoral = votosValidosOriginais.minus(descarte.votosCorrentes).minus(sumVP).divide(numeroDeCadeiras.minus(sumP));
        if (!novoQuocienteEleitoral.equals(quocienteEleitoral))
           System.out.println("quocienteEleitoral reduzido para "+quocienteEleitoral);        
        quocienteEleitoral = novoQuocienteEleitoral;
        
        HashSet<Candidato> candidatosComVotosARepassar = new HashSet();
        candidatosComVotosARepassar.addAll(recemEliminados);
        for (Candidato c:eleitos.valueCollection())
           if (c.votosCorrentes.compareTo(quocienteEleitoral)>=0) 
              candidatosComVotosARepassar.add(c);
        
        for (Candidato c:candidatosComVotosARepassar) 
        {
            IRational votosRepassaveis =  (c.status==Candidato.ST_ELIMINADO) ? c.votosCorrentes : c.votosCorrentes.minus(quocienteEleitoral);
            for (Relacao r:c.vizinhosCorrentes.valueCollection())
            {    
                    r.relacionado.votosCorrentes = r.relacionado.votosCorrentes.plus(votosRepassaveis.times(r.percentualRepasse));
                    if (r.relacionado.status!=Candidato.ST_REMANESCENTE && r.relacionado.status!=Candidato.ST_DESCARTE)
                        throw new RuntimeException("Repasse para cadidato não remanescente");
            }        
            c.votosCorrentes = c.votosCorrentes.minus(votosRepassaveis);
            //if (!c.votosCorrentes.equals(zero))
            //    System.out.println("RedePoliticaR2 break");
        }    
        
        //if (numRep++ % 1 ==0)
        //   repassaPelaRedeEAtualizaCoeficienteEstruturaOriginal(dt);
            //System.out.println("Repasee teste");
    }        
    
    public static void main(String[] args)
    {
    }
}
