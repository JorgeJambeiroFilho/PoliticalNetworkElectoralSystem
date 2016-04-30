/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package redepolitica4;

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

    Relacao(Relacao r)
    {
        this.relacionado = r.relacionado;
        this.percentualRepasse = r.percentualRepasse;
    }

    public Candidato getRelacionado()
    {
        return relacionado;
    }

    public IRational getPercentualRepasse()
    {
        return percentualRepasse;
    }

    public String toString()
    {
        return relacionado.numero+" "+percentualRepasse;
    }        
    
}
