/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package deputados2012;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import redepolitica2.AplicaRedeAEleicaoRealizadaViaPartidos;

/**
 *
 * @author jesjf
 */
public class DadosCandidatoEleicaoPassada
{
    String nome;
    public int numero;
    public String partido;
    public String coligacao;
    public int votos;
    public int status;
    public static int e_eleito = 1;
    public static int e_eliminado = 2;
    public static int e_bandeira = 3;

    public DadosCandidatoEleicaoPassada(String nome, int numero, String partido, String coligacao, int votos, String status)
    {
        this.nome = nome;
        this.numero = numero;
        this.partido = partido;
        this.coligacao = coligacao;
        this.votos = votos;
        if (status.equals("eleito"))
        {
            this.status = e_eleito;
        }
        else if (status.equals("eliminado"))
        {
            this.status = e_eliminado;
        }
        else if (status.equals("bandeira"))
        {
            this.status = e_bandeira;
        }
        else
        {
            throw new RuntimeException("Resultado incompreensível");
        }
        if (this.coligacao.isEmpty())
        {
            this.coligacao = this.partido;
        }
    }

    public String toString()
    {
        return nome + " ; " + numero + " ; " + " " + coligacao + " " + votos + " " + status;
    }

    public static List<DadosCandidatoEleicaoPassada> leRecurso(String fileName)
    {
        ArrayList<DadosCandidatoEleicaoPassada> res = new ArrayList();
        try
        {
            String path = DadosCandidatoEleicaoPassada.class.getName().replace(".", "/");
            path = new File(path).getParentFile().getPath().replace("\\", "/");
            URL url = DadosCandidatoEleicaoPassada.class.getClassLoader().getResource(path + "/" + fileName);
            BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()));
            int p = 0;
            //reader.readLine();
            String lin = reader.readLine();
            while (lin != null)
            { 
                lin = lin.trim();
                String[] lis = lin.split(";");
                System.out.println(lin);
                DadosCandidatoEleicaoPassada dcand = new DadosCandidatoEleicaoPassada(lis[0], Integer.parseInt(lis[1]), lis[2], lis[3], Integer.parseInt(lis[4].replace(".", "")), lis[5].toLowerCase());
                res.add(dcand);
                if (dcand.status == DadosCandidatoEleicaoPassada.e_eleito)
                {
                    System.out.println("Eleito " + dcand);
                }
                if (dcand.status == DadosCandidatoEleicaoPassada.e_bandeira)
                {
                    System.out.println("Bandeira " + dcand);
                }
                lin = reader.readLine();
                p++;
                // if (p> 300)
                //     break;
            }
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
        return res;
    }
    
}
