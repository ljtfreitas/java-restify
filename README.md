# Java Restify

*Restify* é uma biblioteca para auxiliar a construção de clientes de API's HTTP, inspirada em projetos como [Feign](https://github.com/OpenFeign/feign/), [Retrofit](https://square.github.io/retrofit/) e [RESTEasy](https://docs.jboss.org/resteasy/docs/3.0.19.Final/userguide/html/RESTEasy_Client_Framework.html#d4e2187).

Restify foi projetado para conectar facilmente seu código às API's que desejar consumir, sem nenhum acoplamento do seu modelo de objetos com detalhes do protocolo HTTP.

> O objeto principal é permitir que você represente API's como *objetos* e as operações disponíveis como *métodos*, aumentando o nível de abstração do seu código.

### Requisitos
* Java 8

### Instalação
##### Maven
```xml
<dependency>
    <groupId>com.github.ljtfreitas</groupId>
    <artifactId>java-restify</artifactId>
    <version>{version}</version>
</dependency>
```
##### Gradle
```groovy
dependencies {
    compile("com.github.ljtfreitas:java-restiy:{version}")
}
```
> A princípio, **nenhuma** dependência adicional será incluída no seu classpath; um princípio de implementação do Restify é utilizar por padrão apenas as classes disponíveis no JDK. Dependências adicionais devem ser incluídas explicitamente. Por exemplo, se você desejar utilizar o [Gson](https://github.com/google/gson) para manipulação de json, você deve adicionar essa dependência no seu projeto.

### Como utilizar
O código abaixo mostra a utilização do Restify para consumir a API do Github.
```java
@Path("https://api.github.com")
public interface GitHub {

    @Path("/repos/{owner}/{repo}/contributors")
	@Get
	public List<Contributor> contributors(String owner, String repo);

}

@JsonIgnoreProperties(ignoreUnknown = true)
static class Contributor {

	private String login;
	private int contributions;

	@Override
	public String toString() {
	    return "Contributor: [" + login + "] - " + contributions + " contributions.";
	}
}

public static void main(String[] args) {

	// Cria o proxy da interface GitHub
	GitHub gitHubApi = new RestifyProxyBuilder()
	    .target(GitHub.class)
	        .build();

	/*
	 A chamada do método "contributors" vai realizar um GET para https://api.github.com/repos/ljtfreitas/java-restify/contributors. O "bind" dos argumentos do método com o path é realizado utilizando o nome dos parâmetros.
	 A resposta da API do GitHub está no formato application/json; o Restify irá automaticamente desserializar o JSON de resposta para o tipo de retorno do método
	 */
	gitHubApi.contributors("ljtfreitas", "java-restify")
	    .forEach(System.out::println);
}
```

Você também pode definir a URL base da API durante a criação do proxy:
```java
GitHub gitHubApi = new RestifyProxyBuilder().target(GitHub.class, "https://api.github.com").build();
```

O endpoint completo da requisição é construído concatenando a URL base da API utilizada no builder (se existir, como o exemplo acima), com o conteúdo da anotação ```@Path``` no topo da interface (se existir, como no primeiro exemplo), e anotação ```@Path``` utilizada no método (que é o único local obrigatório).

O método HTTP utilizado na requisição também é obrigatório; no exemplo anterior é utilizada a anotação ```@Get``` para utiizarmos o método GET. Outras anotações/métodos estão disponíveis: ```@Post```, ```@Put```, ```@Delete```, ```@Head```, ```@Options```, ```@Path```, ```@Head``` e ```@Trace```. Se você precisar utilizar outros métodos HTTP, também pode anotar o método da sua interface com ```@Method(name = "name")```.

### Path dinâmico
No primeiro exemplo, o *path* informado no método possuía partes variáveis:
```java
@Path("/repos/{owner}/{repo}/contributors")
@Get
public List<Contributor> contributors(String owner, String repo);
```
O *path* define as variáveis **owner** e **repo**, e o valor dessas variáveis é definido no momento da invocação do método. Por padrão, o *bind* será realizado utilizando os **nomes** dos argumentos do método.

> Para permitir que os nomes dos parâmetros dos métodos sejam obtidos através de *reflection*, você deve compilar o seu código com a flag **-parameters**

Caso prefira não utilizar o *bind* pelo nome dos argumentos ou quiser associar a varíavel a um nome diferente do nome do parâmetro, você pode utilizar a anotação ```@PathParameter```:
```java
@Path("/repos/{owner}/{repo}/contributors")
@Get
public List<Contributor> contributors(@PathParameter("owner") String ownerName, @PathParameter("repo") String repoName);
```

Você pode incluir essa anotação nos seus argumentos mesmo sem customizar o nome, explicitando que esses parâmetros fazem parte do *path*:
```java
@Path("/repos/{owner}/{repo}/contributors")
@Get
public List<Contributor> contributors(@PathParameter String owner, @PathParameter String repo);
```

> O Restify irá considerar o parâmetro como uma varíavel integrante do *path*, se nenhuma anotação for adicionada.

### Cabeçalhos
Você pode enviar cabeçalhos HTTP utilizando a anotação ```@Header```:
```
@Header(name = "X-Custom-Header", value = "custom header")
public interface MyApi {

    @Path("/customers/{id}") @Get
	@Header(name = "Accept", value = "application/json")
	public Customer findCustomerById(String id);
}
```
As anotações ```@Header``` da interface e do método são mergeadas no momento da construção da requisição. No exemplo acima, ao invocar o método ```findCustomerBy```, a requisição terá os cabeçalhos ```X-Custom-Header```(que será aplicado a todos os métodos da classe) e ```Accept```.

A anotação ```@Header``` é *repetível*, de modo que você pode informar vários cabeçalhos (no topo da interface ou por método):

```
@Header(name = "X-Custom-Header", value = "custom header")
@Header(name = "X-Other-Custom-Header", value = "custom header")
public interface MyApi {

	@Path("/customers/{id}") @Get
	@Header(name = "Accept", value = "application/json")
	@Header(name = "X-Custom-Customer-Header", value = "specific custom method header")
		public Customer findCustomerById(String id);
	}
}
```

Você também pode construir a requisição com cabeçalhos dinâmicos, utilizando a anotação ```@HeaderParameter```:
```java
public interface MyApi {
    @Path("/customers/{id}") @Get
    @Header(name = "X-Custom-Customer-Header", value = "{customHeader}")
    public Customer findCustomerById(String id, @HeaderParameter String customHeader);

    @Header(name = "X-Custom-Customer-Header", value = "{customHeaderName}")
	public Customer otherCustomerMethod(@HeaderParameter("customHeaderName") String customHeader);

}
```
O *bind* das variáveis com os parâmetros anotados com ```@HeaderParameter``` funciona da mesma forma que a anotação ```@PathParameter```. Para informar ao Restify que o parâmetro do método deve gerar um HTTP header, a anotação ```@HeaderParameter``` é obrigatória (como discutido anteriormente, se nenhuma anotação for adicionada ao parâmetro, o Restify irá considerar que é um parâmetro integrante do *path*).

Caso precise obter os cabeçalhos da resposta HTTP (por exemplo, em uma requisição do tipo HEAD ou OPTIONS), o seu método pode ter um retorno do tipo ```Headers``` ou ```EndpointResponse``` (ambas as classes fazem parte da API do Restify).

### Query parameters
Você pode utilizar os parâmetros do método para enviar *query parameters* na sua requisição:
```java
public interface MyApi {
	@Path("/customers") @Get
	public Customer findCustomerByName(@QueryParameter String name);

	@Path("/customers") @Get
	public Customer findCustomerByName(@QueryParameter("customer_name") String name);
}
```
Se você quiser enviar múltiplos parâmetros e não quiser sobrecarregar a assinatura do método com vários argumentos, você pode adicionar um único parâmetro do tipo ```Map<String, ?>``` anotado com ```@QueryParameters```
```java
public interface MyApi {

		@Path("/customers") @Get
		public Customer findCustomerByParameters(@QueryParameters Map<String, String> mapParameters);
}

public static void main(String[] args) {

	MyApi myApi = new RestifyProxyBuilder()
	    .target(GitHub.class)
	        .build();

	Map<String, String> mapParameters = new LinkedHashMap<>;
	mapParameters.put("name", "Tiago de Freitas Lima");
	mapParameters.put("age", "31");

	// O endpoint da requisição será construido com a query string:
	// name=Tiago+de+Freitas+Lima&age=31
	Customer customer = myApi.findCustomerByParameters(mapParameters);
}
```
Obrigatoriamente a chave do mapa deve ser do tipo String; os valores podem ser de qualquer tipo, mas o Restify utilizará o método ```toString()``` de cada valor do mapa para gerar a query string (se o valor for um Iterable, o método ```toString``` será aplicado para cada elemento).

Para enviar núltiplos parâmetros, o Restify oferece mais duas opções. Uma delas é o objeto **Parameters**, um simples objeto *Map-like* que permite adicionar múltiplos valores por parâmetro:
```java
public interface MyApi {

	@Path("/customers") @Get
	public Customer findCustomerByParameters(@QueryParameters Parameters parameters);

}

public static void main(String[] args) {

	MyApi myApi = new RestifyProxyBuilder()
	    .target(GitHub.class)
	        .build();

	Parameters parameters = new Parameters();
	parameters.put("name", "Tiago de Freitas Lima");
	parameters.put("age", "31");
	parameters.put("socialPreferences", "facebook");
	parameters.put("socialPreferences", "twitter");

	// O endpoint da requisição será construido com a query string:
	// name=Tiago+de+Freitas+Lima&age=31&socialPreferences=facebook&socialPreferences=twitter
	Customer customer = myApi.findCustomerByParameters(mapParameters);
}
```
Outra possibilidade é enviar um objeto que represente um "formulário" de parâmetros; um objeto anotado com ```@Form```, anotação disponibilizada pelo Restify:
```java
public interface MyApi {

	@Path("/customers") @Get
	public Customer findCustomerByParameters(@QueryParameters FormParameters parameters);

}

@Form
static class FormParameters {

	@Field
	String name;

	@Field("customer_age")
	int age;
}

public static void main(String[] args) {

	MyApi myApi = new RestifyProxyBuilder()
	    .target(GitHub.class)
	        .build();

	FormParameters parameters = new FormParameters();
	parameters.name = "Tiago de Freitas Lima";
	parameters.age = 31;

	// O endpoint da requisição será construido com a query string:
	// name=Tiago+de+Freitas+Lima&customer_age=31
	Customer customer = myApi.findCustomerByParameters(mapParameters);
}
```
### Converters (serialização e deserialização)
Para enviar um objeto no corpo da requisição, você deve:
* Definir o cabeçalho **Content-Type**, utilizando algum dos mime-types suportados
* Anotar o parâmetro do método com ```@BodyParameter```

```java
public interface MyApi {

	@Path("/customers") @Post
	@Header(name = "Content-Type", value = "application/json")
	public Customer createCustomer(@BodyParameter Customer customer);

}
```
A resposta da requisição será desserializada para o tipo de retorno do seu método, de acordo com o cabeçalho **Content-Type** da resposta.

Os mime-types suportados por padrão são:
* *application/json*: para escrita/leitura de json as bibliotecas suportadas são o [Jackson](https://github.com/FasterXML/jackson) e o [Gson](https://github.com/google/gson). O Restify irá analisar qual biblioteca está disponível no classpath (dando preferência ao Jackson) e se nenhuma das duas estiver presente, não será possível enviar ou receber requisições com conteúdo json.
* *application/xml*: utiliza o [JAX-B](https://docs.oracle.com/javase/8/docs/api/javax/xml/bind/package-summary.html) para serialização e desserialização de XML
* *text/plain e text/html*: escrita e leitura de texto simples.
* *application/x-www-form-urlencoded*: a serialização trabalha com os mesmos objetos suportados pela anotação ```@QueryParameters``` explicada anteriormente; os parâmetros serão serializados no formato query string, mas serão enviados no corpo da requisição. A **leitura** de uma resposta com esse mime-type não é suportada.
* *multipart/form-data*: equivalente ao formato application/x-www-form-urlencoded, mas permite o envio de arquivos. Explicado em detalhes mais abaixo.

É possível customizar os mime-types que serão avaliados pelo Restify. Digamos que, por exemplo, a API que você deseja consumir trabalhe apenas com json; você pode configurar isso explicitamente na criação do proxy (utilizando o método ```converters()``` do ```RestifyProxyBuilder```)
```java
MyApi myApi = new RestifyProxyBuilder()
			.converters()
				.json()
				.and()
			.target(MyApi.class).build();
```
Outras opções disponíveis são ```xml()```, ```form()``` e ```all()``` (que é a opção padrão). Se desejar criar um converter próprio, ou para algum mime-type não suportado pelo Restify, você pode criar um serializador customizado,  implementando ```HttpMessageWriter``` (para gerar o corpo da requisição) e ```HttpMessageReader``` (para converter a resposta da requisição para os seus objetos), e utilizá-lo no builder:
```java
MyApi myApi = new RestifyProxyBuilder()
			.converters()
				.add(new MyContentTypeConverter())
			.target(MyApi.class).build();
```
Ou, se quiser combinar o seu próprio converter com as demais opções já disponíveis:
```java
MyApi myApi = new RestifyProxyBuilder()
			.converters()
				.add(new MyContentTypeConverter())
				.all()
			.target(MyApi.class).build();
```

##### multipart/form-data (upload de arquivos)
Para enviar um arquivo através da requisição http, você deve:
* Definir o content-type da requisição para *multipart/form-data*
* Anotar o parâmetro do método com ```@BodyParameter```

O tipo do parâmetro pode ser:
* um objeto do tipo ```MultipartFile``` (objeto fornecido pelo Restify)
```java
public interface MyApi {

	@Path("/upload") @Post
	@Header(name = "Content-Type", value = "multipart/form-data ")
	public void upload(@BodyParameter MultipartFile file);

}

public static void main(String[] args) {

		MyApi myApi = new RestifyProxyBuilder().target(MyApi.class)
		    .build();

		MultipartFile file = MultipartFile.create("file", new File("/path/to/file.txt"));

		myApi.upload(file);
}
```
* um objeto anotado com ```@MultipartForm``` (anotação fornecida pelo Restify). Essa anotação tem uma semântica equivalente à anotação ```@Form``` discutida anteriormente, servindo para marcar um objeto que representa um "formulário" de parâmetros, podendo conter campos anotados com ```@MultipartField``` (o arquivo a ser enviado ao servidor)
```java
public interface MyApi {

	@Path("/upload") @Post
	@Header(name = "Content-Type", value = "multipart/form-data ")
	public void upload(@BodyParameter MultipartFormParameters parameters);

}

@MultipartForm
static class MultipartFormParameters {

	@Field
	String name;

	@MultipartField
	File file;
}

public static void main(String[] args) {

	MyApi myApi = new RestifyProxyBuilder().target(MyApi.class).build();

	MultipartFormParameters parameters = new MultipartFormParameters();
	parameters.name = "Tiago de Freitas Lima";
	parameters.file = new File("/path/to/file.txt");

	myApi.upload(parameters);
}
```
* um objeto do tipo com ```MultipartParameters``` (fornecido pelo Restify). Esse objeto tem uma semântica equivalente ao ```Parameters``` discutido anteriormente, sendo um objeto *Map-like* que representa um conjunto de parâmetros (podendo conter múltiplos valores por parâmetro), permitindo também o envio de arquivos.
```java
public interface MyApi {

	@Path("/upload") @Post
	@Header(name = "Content-Type", value = "multipart/form-data ")
	public void upload(@BodyParameter MultipartParameters parameters);

}

public static void main(String[] args) {

	MyApi myApi = new RestifyProxyBuilder().target(MyApi.class).build();

	MultipartParameters parameters = new MultipartParameters();
	parameters.put("name", "Tiago de Freitas Lima");
	parameters.put("file", new File("/path/to/file.txt"));

	myApi.upload(parameters);
}
```
* um ```Map<String, ?>```, caso queira enviar múltiplos campos.
```java
public interface MyApi {

	@Path("/upload") @Post
	@Header(name = "Content-Type", value = "multipart/form-data ")
	public void upload(@BodyParameter Map<String, Object> parameters);

}

public static void main(String[] args) {

	MyApi myApi = new RestifyProxyBuilder().target(MyApi.class).build();

	Map<String, Object> parameters = new HashMap<>();
	parameters.put("name", "Tiago de Freitas Lima");
	parameters.put("file", new File("/path/to/file.txt"));

	myApi.upload(parameters);
}
```

### Tratamento de erro
O tratamento de erro padrão do Restify é lançar uma exceção do tipo ```RestifyHttpException``` para qualquer resposta cujo status HTTP não seja 2xx. Caso queira customizar esse comportamento, você pode utilizar o método ```error()``` do builder para enviar uma implementação customizada da interface ```EndpointResponseErrorFallback```:
```java
MyApi myApi = new RestifyProxyBuilder()
				.error(new MyCustomErrorFallback())
				.target(MyApi.class).build();
```
O método ```onError``` desse objeto será invocado sempre que o status HTTP da resposta for diferente de 2xx; sua implementação customizada poderia, por exemplo, gerar um retorno de método padrão para respostas de erro, ou ler o corpo da resposta para lançar uma exceção mais adequada do seu próprio domínio.

##### Respostas 404 (Not Found)
Um caso especial é o código de erro 404 (Not Found). Uma API REST bem projetada provavelmente implementará a semântica de devolver *Not Found* caso o recurso requerido não exista. Por exemplo:
```java
public interface MyApi {

    @Path("/customers/{id}") @Get
	public Customer findCustomerById(String id);
}
```
Imaginemos que o método acima seja invocado com o id "1", e esteja seja um id inexistente no servidor. Nossa intenção é que o método acima devolva ```null``` ao invés de lançar uma exceção. O Restify permite uma customização específica para este cenário:
```java
MyApi myApi = new RestifyProxyBuilder()
				.error()
				    .emptyOnNotFound()
				.target(MyApi.class).build();
```
O método ```emptyOnNotFound()``` configura o tratamento de erro para devolver ```null```, caso a resposta da requisição seja 404 (outros códigos não-2xx continuam operando da mesma forma). Se preferir ser ainda mais cuidadoso ao lidar com o *null*, seu método pode retornar ```java.util.Optional```:
```java
public interface MyApi {

    @Path("/customers/{id}") @Get
	public Optional<Customer> findCustomerById(String id);
}
```

### Client HTTP
Por padrão as requisições HTTP são realizadas utilizando [```HttpUrlConnection```](https://docs.oracle.com/javase/8/docs/api/index.html?java/net/HttpURLConnection.html) , disponível no próprio JDK. Outras implementações já existentes são:
* [ApacheHttpClient](https://hc.apache.org/httpcomponents-client-ga/index.html)
```java
MyApi myApi = new RestifyProxyBuilder()
				.client(new ApacheHttpClientRequestFactory())
				.target(MyApi.class).build();
```
* [OkHttp](http://square.github.io/okhttp/)
```java
MyApi myApi = new RestifyProxyBuilder()
				.client(new OkHttpClientRequestFactory())
				.target(MyApi.class).build();
```
(Lembrete: para utilizar essas bibliotecas você deve adicioná-las no seu classpath.)

### Interceptors
O Restify permite adicionar *interceptors* que são executados antes da requisição HTTP ser realizada. Você pode utilizar esse recurso para alterar detalhes da requisição como, por exemplo, adicionar cabeçalhos ou query parameters dinamicamente, ou adicionar informações de autenticação. Para configurar interceptadores, você deve utilizar o método ```interceptors()``` do ```RestifyProxyBuilder```.

Para adicionar seus interceptors, implemente a interface ```EndpointRequestInterceptor``` e configure o proxy com a sua implementação:
```java
MyApi myApi = new RestifyProxyBuilder()
                .interceptors()
				    .add(new MyCustomInterceptor())
				    .and()
				.target(MyApi.class).build();
```

O Restify disponibiliza um interceptor para gerar o cabeçalho **Accept**:
```java
//a configuração abaixo aplica o cabeçalho Accept=application/json para todas as requisições
MyApi myApi = new RestifyProxyBuilder()
                .interceptors()
				    .accept("application/json")
				    .and()
				.target(MyApi.class).build();
```
###### Autenticação
Para requisições que exigem autenticação, o Restify disponibiliza uma interface chamada *Authentication* que possui um único método, *content*, que devolve o conteúdo que deve ser incluído no cabeçalho **Authorization**. O Restify fornece uma implementação para autenticação Basic:
```java
MyApi myApi = new RestifyProxyBuilder()
                .interceptors()
				    .authorization(new BasicAuthentication("user", "password"))
				    .add()
				.target(MyApi.class).build();
```
Caso precise de um modelo diferente de autenticação, basta criar a sua implementação da interface Authentication e passar ao método ```authorization```.

### Requisições assíncronas
O Restify suporta requisições assíncronas por padrão. A configuração padrão do ```RestifyProxyBuilder``` utiliza um pool de threads criado com [```Executors.newCachedThreadPool()```](https://docs.oracle.com/javase/8/docs/api/java/util/concurrent/Executors.html#newCachedThreadPool--). Se essa configuração não atender as necessidades da sua aplicação você pode utilizar o seu próprio [```Executor```](https://docs.oracle.com/javase/8/docs/api/index.html?java/util/concurrent/Executor.html) facilmente:
```java
ExecutorService executor = Executors.newFixedThreadPool(10);

MyApi myApi = new RestifyProxyBuilder()
	    .executables()
	        .async(executor)
            .and()
        .target(MyApi.class).build();
```
Os métodos da sua interface podem seguir diferentes formatos:
```java
public interface MyApi {

	// retornar um Future parametrizado com o objeto de resposta esperado
	@Path("/customers/{id}") @Get
	public Future<Customer> findCustomerById(@PathParameter String id);

	// retornar um FutureTask parametrizado com o objeto de resposta esperado
	@Path("/customers/{id}") @Get
	public FutureTask<Customer> findCustomerById(@PathParameter String id);

	// retornar um CompletableFuture parametrizado com o objeto de resposta esperado
	@Path("/customers/{id}") @Get
	public CompletableFuture<Customer> findCustomerById(@PathParameter String id);

	// retornar um objeto do tipo AsyncEndpointCall (fornecido pelo Restify)
	@Path("/customers/{id}") @Get
	public AsyncEndpointCall<Customer> findCustomerById(@PathParameter String id);
}
```
Seus métodos também podem retornar objetos do tipo [```Runnable```](https://docs.oracle.com/javase/8/docs/api/index.html?java/lang/Runnable.html) ou [```Callable```](https://docs.oracle.com/javase/8/docs/api/index.html?java/util/concurrent/Callable.html); nesses casos, você será responsável por associar esses objetos com a thread onde eles serão executados.

Você também pode trabalhar com parâmetros de callback, ao invés de lidar com o retorno do método. O Restify fornece a anotação ```@CallbackParameter```:
```java
public interface MyApi {

	@Path("/customers/{id}") @Get
	public void findCustomerById(@PathParameter String id, @CallbackParameter EndpointCallCallback<Customer> callback);
```
A interface ```EndpointCallCallback``` deve ser parametrizada com o retorno esperado da sua requisição; essa interface extende outras duas, ```EndpointCallSuccessCallback``` e ```EndpointCallFailureCallback```. Se você desejar implementar apenas um destes callbacks (apenas o caso de sucesso, ou apenas o caso de erro), você também pode utilizá-los na assinatura do método:
```java
public interface MyApi {

	@Path("/customers/{id}") @Get
	public void findCustomerById(@PathParameter String id, @CallbackParameter EndpointCallCallback<Customer> callback);

	@Path("/customers/{id}") @Get
	public void findCustomerById(@PathParameter String id, @CallbackParameter EndpointCallSuccessCallback<Customer> successCallback);

	@Path("/customers/{id}") @Get
	public void findCustomerById(@PathParameter String id, @CallbackParameter EndpointCallFailureCallback errorCallback);

	@Path("/customers/{id}") @Get
	public void findCustomerById(@PathParameter String id, @CallbackParameter EndpointCallSuccessCallback<Customer> successCallback,
			@CallbackParameter EndpointCallFailureCallback errorCallback);
}
```
Outro tipo de ```@CallbackParameter``` suportado é um argumento do tipo [```BiConsumer```](https://docs.oracle.com/javase/8/docs/api/index.html?java/util/function/BiConsumer.html), onde o primeiro parâmetro é o tipo esperado da resposta e o segundo é um [```Throwable```](https://docs.oracle.com/javase/8/docs/api/index.html?java/lang/Throwable.html):
```java
public interface MyApi {

	@Path("/customers/{id}") @Get
	public void findCustomerById(@PathParameter String id, @CallbackParameter BiConsumer<Customer, Throwable> callback);

}
```
