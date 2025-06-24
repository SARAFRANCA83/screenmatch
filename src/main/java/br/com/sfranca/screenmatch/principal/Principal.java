package br.com.sfranca.screenmatch.principal;

import br.com.sfranca.screenmatch.model.DadosSerie;
import br.com.sfranca.screenmatch.model.DadosTemporada;
import br.com.sfranca.screenmatch.model.Episodio;
import br.com.sfranca.screenmatch.model.Serie;
import br.com.sfranca.screenmatch.repository.SerieRepository;
import br.com.sfranca.screenmatch.service.ConsumoApi;
import br.com.sfranca.screenmatch.service.ConverteDados;

import java.util.*;
import java.util.stream.Collectors;

import static org.hibernate.internal.util.collections.ArrayHelper.forEach;

public class Principal {
    private Scanner leitura = new Scanner(System.in);
    private ConsumoApi consumo = new ConsumoApi();
    private ConverteDados conversor = new ConverteDados();
    private final String ENDERECO = "https://www.omdbapi.com/?t=";

    private final String API_KEY = "&apikey=6585022c";
    private List<DadosSerie> dadosSerie = new ArrayList<>();
    private SerieRepository repositorio;
    private List<Serie> series = new ArrayList<>();

    public Principal(SerieRepository repositorio) {
        this.repositorio = repositorio;
    }

    public void exibeMenu() {
        var opcao = -1;
        while (opcao != 0) {
            var menu = """
                    1 - Buscar séries
                    2 - Buscar episódios
                    3 - Listar séries buscadas
                    4 - Buscar série por título
                    5 - Buscar séries por ator
                    6 - Top 5 Séries
                    
                    0 - Sair                                 
                    """;

            System.out.println(menu);
            opcao = leitura.nextInt();
            leitura.nextLine();

            switch (opcao) {
                case 1:
                    buscarSerieWeb();
                    break;
                case 2:
                    buscarEpisodioPorSerie();
                    break;
                case 3:
                    listarSeriesBuscadas();
                    break;
                case 4:
                    buscarSeriePorTitulo();
                case 5:
                    burcarSeriesPorAtor();
                    break;
                case 6:
                    buscarTop5Series();
                    break;
                case 0:
                    System.out.println("Saindo...");
                    break;
                default:
                    System.out.println("Opção inválida");
            }
        }
    }



    private void buscarSerieWeb() {
        try {
            DadosSerie dados = getDadosSerie();
            Serie serie = new Serie(dados);
            repositorio.save(serie);
            System.out.println(dados);
        } catch (Exception e) {
            System.out.println("Erro ao criar ou salvar a série: " + e.getMessage());
        }
    }

    private DadosSerie getDadosSerie() {
        System.out.println("Digite o nome da série para busca");
        var nomeSerie = leitura.nextLine();
        var json = consumo.obterDados(ENDERECO + nomeSerie.replace(" ", "+") + API_KEY);
        DadosSerie dados = conversor.obterDados(json, DadosSerie.class);
        return dados;
    }

    private void buscarEpisodioPorSerie() {
        listarSeriesBuscadas();
        System.out.println("Escolha uma série pelo nome: ");
        var nomeSerie = leitura.nextLine();

        Optional<Serie> serie = repositorio.findByTituloContainingIgnoreCase(nomeSerie);
        if (serie.isPresent()) {

            var serieEncontrada = serie.get();
            List<DadosTemporada> temporadas = new ArrayList<>();

            for (int i = 1; i <= serieEncontrada.getTotalTemporadas(); i++) {
                var json = consumo.obterDados(ENDERECO + serieEncontrada.getTitulo().replace(" ", "+") + "&season=" + i + API_KEY);
                DadosTemporada dadosTemporada = conversor.obterDados(json, DadosTemporada.class);
                temporadas.add(dadosTemporada);
            }
            temporadas.forEach(System.out::println);

            List<Episodio> episodios = temporadas.stream()
                    .flatMap(d -> d.episodios().stream()
                            .map(e -> new Episodio(d.numero(), e)))
                    .collect(Collectors.toList());
            serieEncontrada.setEpisodios(episodios);
            repositorio.save(serieEncontrada);
        } else {
            System.out.println("Série não encontrada! ");
        }
    }

    private void listarSeriesBuscadas() {
        series = repositorio.findAll();
        series.stream()
                .sorted(Comparator.comparing(Serie::getGenero))
                .forEach(System.out::println);

    }

    private void buscarSeriePorTitulo() {
        System.out.println("Digite um trecho do título da série: ");
        //var nomeSerie = leitura.nextLine();
        String nomeSerie = leitura.nextLine();

        Optional<Serie> serieBuscada = repositorio.findByTituloContainingIgnoreCase(nomeSerie);

        if (serieBuscada.isPresent()) {
            System.out.println("Série encontrada! ");
            System.out.println(serieBuscada.get());
        } else {
            System.out.println("Série não encontrada !");

        }

    }
    private void burcarSeriesPorAtor() {
        System.out.println("Qual nome para busca? ");
        var nomeAtor = leitura.nextLine();
        System.out.println("Avaliações a partir de que valor? ");
        var avaliacao = leitura.nextDouble();
        List<Serie> seriesEncontradas = repositorio.findByAtoresContainingIgnoreCaseAndAvaliacaoGreaterThanEqual(nomeAtor, avaliacao);

        if (seriesEncontradas.isEmpty()) {
            System.out.println("Nenhuma série encontrada com o ator " + nomeAtor + ".");
        } else {
            System.out.println("Séries em que o " + nomeAtor + " trabalhou:");
            seriesEncontradas.forEach(s ->
                    System.out.println(s.getTitulo() + " - avaliação: " + s.getAvaliacao()));
        }
    }
    private void buscarTop5Series() {
        System.out.println("Quais as séries você quer? ");
        List<Serie> serieTop = repositorio.findTop5ByOrderByAvaliacaoDesc();
        serieTop.forEach(s ->
                System.out.println(s.getTitulo() + " - avaliação: " + s.getAvaliacao()));
    }

}


