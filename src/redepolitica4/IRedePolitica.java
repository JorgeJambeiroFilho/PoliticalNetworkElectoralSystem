/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package redepolitica4;

import gnu.trove.map.hash.THashMap;
import irational.IRational;
import java.util.HashMap;

/**
 *
 * @author jesjf
 */
public interface IRedePolitica
{

    void addBandeira(int numero);

    void addCandidato(int numero);

    void addRelacao(int numeroCandidatoOrigem, int numeroCandidatoVizinho, IRational percentual);

    void defineCadeiras(int numeroDeCadeiras);

    void defineVotos(int numero, int votosProprios);

    boolean equals(Object o);

    Candidato getCandidato(int numero);

    THashMap<Integer, Candidato> getEleitos();

    void preparaCandidatos();

    void realizaApuracao();

    void removeDosConjuntosDeVizinhos(int num);
    
    void checa(boolean hasJustElected);
    
    void preparaComoSeFosseApurar();

    void close();
    
}
