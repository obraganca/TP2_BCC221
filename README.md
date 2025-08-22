# Jogo TERMO (Trabalho Prático - BCC 221)

Este projeto é uma implementação em Java com interface gráfica (Swing) do popular jogo de palavras TERMO (uma versão brasileira do Wordle). O desenvolvimento foi realizado para a disciplina de Programação Orientada a Objetos (BCC 221).

O jogo inclui um sistema de login/cadastro de usuários, persistência de estatísticas e uma interface responsiva.

## Autores

* Gabriel Barony Menezes
* Marco Antônio Diniz Silva
* Samuel Braga Marques
* Thayllon Ryan Bragança de Almeida
* Thiago Martins Zanete

## Pré-requisitos

Antes de começar, garanta que você tenha os seguintes softwares instalados em sua máquina:

* **Java Development Kit (JDK)** - Versão 8 ou superior.
* **Apache Maven** - Para gerenciamento de dependências e automação do build.
* **Arquivo de Palavras** - Um arquivo de texto (`.txt`) contendo as palavras válidas para o jogo, uma por linha. Por exemplo, um arquivo chamado `palavras.txt`.

## Como Compilar e Executar

Siga os passos abaixo para rodar o projeto localmente.

**1. Descompacte o Projeto e Navegue até a Pasta**

Primeiro, descompacte o arquivo `.zip` do projeto. Em seguida, abra um terminal (CMD, PowerShell, etc.) e utilize o comando `cd` para navegar até o diretório raiz do projeto descompactado (a pasta que contém o arquivo `pom.xml`).

**2. Compile o Projeto**

Execute o comando do Maven para compilar todo o código-fonte.
```bash
mvn compile
```

**3. Execute o Jogo**

Para executar, você precisa usar o plugin `exec:java` do Maven e passar o caminho para o seu arquivo de palavras como um argumento.

**Importante:** Substitua `caminho/para/palavras.txt` pelo caminho real do seu arquivo de texto.

```bash
mvn exec:java -Dexec.mainClass="com.termo.TermoApp" -Dexec.args="caminho/para/palavras.txt"
```

* **Exemplo no Windows:**
    ```bash
    mvn exec:java -Dexec.mainClass="com.termo.TermoApp" -Dexec.args="C:\Users\SeuUsuario\Documentos\palavras.txt"
    ```
* **Exemplo no Linux/Mac:**
    ```bash
    mvn exec:java -Dexec.mainClass="com.termo.TermoApp" -Dexec.args="/home/seu-usuario/documentos/palavras.txt"
    ```

Ao executar o comando, a janela do jogo será aberta.

## Como Jogar

1.  **Login/Cadastro:** Ao iniciar, uma tela de login aparecerá. Digite um nome de usuário e senha. Se o usuário não existir, um novo perfil será criado.
2.  **Objetivo:** Adivinhe a palavra secreta de 5 letras em até 6 tentativas.
3.  **Feedback das Cores:**
    * 🟩 **Verde:** A letra está na palavra e na posição correta.
    * 🟨 **Amarelo:** A letra está na palavra, mas na posição errada.
    * ⬛ **Cinza Escuro:** A letra não faz parte da palavra.
4.  **Estatísticas:** Você pode ver seu progresso, incluindo percentual de vitórias e sequências, clicando no ícone de gráfico.
