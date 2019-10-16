# ActiveMQ-RabbitMQ-chat


###  Application javaFX de chat qui se compose de deux packages.

#### -  premier package: l'application de chat qui utilise l'api JMS et l'implementation ActiveMQ

#### -deuxieme package: l'application de chat qui utilise le protole AMQP a traver le broker RabbitMQ

pour le cas d'utilisation il suffit d'executer l'application en plusieur instances pour simuler deux client avec deux codes differents 
et essayer d'envoyer des message aux autres clients grace au champ text (To).
