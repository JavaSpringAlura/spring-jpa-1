package com.aluracursos.screenmatch.principal;

import com.aluracursos.screenmatch.model.*;
import com.aluracursos.screenmatch.repository.SerieRepository;
import com.aluracursos.screenmatch.service.ConsumoAPI;
import com.aluracursos.screenmatch.service.ConvierteDatos;

import java.util.*;
import java.util.stream.Collectors;

public class Principal {
    private Scanner teclado = new Scanner(System.in);
    private ConsumoAPI consumoApi = new ConsumoAPI();
    private final String URL_BASE = "https://www.omdbapi.com/?t=";
    private final String API_KEY = "&apikey=b5e0f28f";
    private ConvierteDatos conversor = new ConvierteDatos();
    private List<DatosSerie> datosSeries = new ArrayList<>();
    private SerieRepository repositorioSerie;
    private List<Serie> series;

    public Principal(SerieRepository serieRepository) {
        this.repositorioSerie = serieRepository;
    }

    public void muestraElMenu() {
        var opcion = -1;
        while (opcion != 0) {
            var menu = """
                    1 - Buscar series 
                    2 - Buscar episodios
                    3 - Mostrar series buscadas
                    4 - Buscar serie por titulo
                    5 - Buscar serie por temporadas
                    6 - Top 5 series
                    7 - Buscar por Género
                    8 - Filtrar series                             
                    0 - Salir
                    """;
            System.out.println(menu);
            opcion = teclado.nextInt();
            teclado.nextLine();

            switch (opcion) {
                case 1:
                    buscarSerieWeb();
                    break;
                case 2:
                    buscarEpisodioPorSerie();
                    break;
                case 3:
                    mostrarSeriesBuscadas();
                    break;
                case 4:
                    buscarSeriePorTitulo();
                    break;
                case 5:
                    buscarSeriePorTotalTemporadas();
                    break;
                case 6:
                    top5series();
                    break;
                case 7:
                    buscarPorGenero();
                    break;
                case 8:
                    filtrarSeriesPorTemporadaYEvaluacion();
                    break;
                case 0:
                    System.out.println("Cerrando la aplicación...");
                    break;
                default:
                    System.out.println("Opción inválida");
            }
        }

    }

    private void mostrarSeriesBuscadas() {
        series = repositorioSerie.findAll();
        series.stream()
                    .sorted(Comparator.comparing(Serie::getGenero))
                    .forEach(System.out::println);
    }

    private DatosSerie getDatosSerie() {
        System.out.println("Escribe el nombre de la serie que deseas buscar");
        var nombreSerie = teclado.nextLine();
        var json = consumoApi.obtenerDatos(URL_BASE + nombreSerie.replace(" ", "+") + API_KEY);
        System.out.println(json);
        DatosSerie datos = conversor.obtenerDatos(json, DatosSerie.class);
        return datos;
    }
    private void buscarEpisodioPorSerie() {
        mostrarSeriesBuscadas();
        System.out.println("Ingresa el nombre de la serie para mostrar sus episodios: ");
        String nombreSerie = teclado.nextLine();
        Optional<Serie> serie = series.stream()
                        .filter(s -> s.getTitulo().toLowerCase().contains(nombreSerie.toLowerCase()))
                        .findFirst();
        if (serie.isPresent()) {
            var serieEncontrada = serie.get();
            List<DatosTemporadas> temporadas = new ArrayList<>();

            for (int i = 1; i <= serieEncontrada.getTotalTemporadas(); i++) {
                var json = consumoApi.obtenerDatos(URL_BASE + serieEncontrada.getTitulo().replace(" ", "+") + "&season=" + i + API_KEY);
                DatosTemporadas datosTemporada = conversor.obtenerDatos(json, DatosTemporadas.class);
                temporadas.add(datosTemporada);
            }
            temporadas.forEach(System.out::println);

            List<Episodio> episodios =  temporadas.stream()
                    .flatMap(t -> t.episodios().stream()
                            .map(e -> new Episodio(t.numero(),e)))
                    .collect(Collectors.toList());

            serieEncontrada.setEpisodios(episodios);
            repositorioSerie.save(serieEncontrada);

        }


    }
    private void buscarSerieWeb() {
        DatosSerie datos = getDatosSerie();
        Optional<Serie> serie = Optional.of(new Serie(datos));
        if (serie.isPresent()) {
            Serie serieResult = serie.orElse(new Serie(datos));
            System.out.println(serie);
            repositorioSerie.save(serieResult);
        }else{
            System.out.println("La serie buscada no existe en la base de datos de OMDB");
        }

        System.out.println(datos);
    }
    private void buscarSeriePorTitulo() {
        System.out.println("Escribe el titulo de la serie: ");
        String serieBuscada = teclado.nextLine();
        Optional<Serie> serie = repositorioSerie.findByTituloContainsIgnoreCase(serieBuscada);
        if (serie.isPresent()) {
            System.out.println(serie);
        }else{
            System.out.println("No existe el serie");
        }
    }
    private void buscarSeriePorTotalTemporadas() {
        System.out.println("Filtra series con temporadas mayores a: ");
        Integer serieBuscada = Integer.valueOf(teclado.nextLine());
        Optional<List<Serie>> series = repositorioSerie.findByTotalTemporadasGreaterThanEqual(serieBuscada);
        if (series.isPresent()) {
            List<Serie> seriesList = series.orElse(new ArrayList<>());
            seriesList.forEach(System.out::println);
        }else{
            System.out.println("No existe el serie");
        }
    }
    private void top5series(){
        // en caso haya menos series que el top traera todas las series sin errores ordenadas por la evaluacion desendentenemente
        List<Serie> series = repositorioSerie.findTop5ByOrderByEvaluacionDesc();
        series.forEach(System.out::println);
    }
    private void buscarPorGenero(){
        System.out.println("Escribe el genero de la serie: ");
        var input = teclado.nextLine();
        var genero = Categoria.fromEspaniol(input);
        List<Serie> series = repositorioSerie.findByGenero(genero);
        series.forEach(System.out::println);
    }
    private void filtrarSeriesPorTemporadaYEvaluacion(){
        System.out.print("Ingrese las temporadas máximas: ");
        Integer temporadas = Integer.valueOf(teclado.nextLine());
        System.out.print("Ingrese la evaluación minimas: ");
        Double evaluacion = Double.valueOf(teclado.nextLine());
        Optional<List<Serie>> series = repositorioSerie.findByTotalTemporadasLessThanEqualAndEvaluacionGreaterThanEqual(temporadas, evaluacion);
        if (series.isPresent()) {
            List<Serie> seriesList = series.orElse(new ArrayList<>());
            seriesList.forEach(System.out::println);
        }else{
            System.out.println("No hay series con: "+ temporadas + " temporadas y evaluacion: "+ evaluacion);
        }
    }

}

