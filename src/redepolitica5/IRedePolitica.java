/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package redepolitica5;

import redepolitica4.*;
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

    HashMap<Integer, Candidato> getEleitos();

    void preparaCandidatos();

    void realizaApuracao();

    void removeDosConjuntosDeVizinhos(int num);
    
}
