/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package redepolitica5;

import redepolitica4.*;
import irational.IRational;

/**
 *
 * @author jesjf
 */
public class Relacao
{
    Candidato relacionado;
    IRational percentualRepasse;

    public Relacao(Candidato relecionado, IRational percentualRepasse)
    {
        this.relacionado = relecionado;
        this.percentualRepasse = percentualRepasse;
    }

    public Candidato getRelacionado()
    {
        return relacionado;
    }

    public IRational getPercentualRepasse()
    {
        return percentualRepasse;
    }
    
}
