Projeto de SC - Fase 1
Autores: 
    Francisco Teixeira | FC56305
    Afonso Soares | FC56314
    Gonçalo Correia | FC56316

Limitações: Pelos nossos testes o projeto não se encontra com limitações

Como executar:
    Server: 
        java -jar Server.jar <porto>
            - <porto> pode ser qualquer porto especificado, por omissão o server usa o porto 12345
            Exemplo: java TintolmarketServer 12345
    Cliente:
        java -jar Client.jar <serverAddress> <username> <password>
            - <serverAddress> pode ser apenas um ip e por omissão o porto será 12345 ou pode se especificar o porto
                usando ":", poranto <serverAddress> ficaria <ip>:<porto>
            - <username> o username do cliente
            - <password> a password do cliente
            Exemplo(com especificação do porto) : java Tintolmarket 127.0.0.1:12345 User password
            Exemplo(sem especificação do porto) : java Tintolmarket 127.0.0.1 User password

    Notas sobre a execução:

        1 -> Ao se autenticar um novo cliente, uma nova pasta chamada de "client<username>DataBase" será criada,
                representa a base de dados que diz respeito a esse user, se quisermos fazer um "add" de um novo vinho,
                a imagem que diz respeito a esse vinho tem de ser posta previamente nessa pasta antes de se fazer o comando
                add.
        2 -> O server tem uma pasta chamada "serverBase" que representa a sua base de dados, esta pasta NÃO PODE SER APAGADA já que
                a mesma é crucial para o programa funcionar, é onde o server guarda as suas imagens e os ficherios txt com o estado do progrma