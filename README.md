# Labo_Kraan

## Datastructuur

De datastructuur voor de slots is volledig onverandert gebleven. De enige verandering is de toevoeging van een todo lijst aan de Gantry klasse. Deze todo lijst zal acties bevatten die door de gegeven gantry nog zullen worden uitgevoerd. Deze todo zullen in een FIFO queue worden bijgehouden onder de vorm van integer arrays en een volgende slot waar de gantry naar dient te bewegen. Deze hebben steeds dezelfde vorm, namelijk:
[ item id, slot id, item priority ]
Indien een gantry reeds in het bezit is van een item zal deze de item droppen op het slot met de gegeven id, anders zal hij de item uit het opgegeven slot nemen. Hier komt de bidirectionele koppeling zeker van pas, aangezien de slotlijst continu gesorteerd wordt en de indexen dus niet meer overeenkomen met de ids. Bij de items is dit wel het geval. Aangezien bij elke opgegeven job een item gegegeven is kunnen we er het eindslot, voor een input, of het beginslot, in het geval van een output uit bepalen. Voor de output laat dit ons ook weten dat de eerst volgende output nog niet kan afgehandeld worden, namelijk wanneer de item een null slot heeft.

## Principe algoritme

Dit algoritme werkt door steeds een "tick" uit te voeren tot de input en output volledig verwerkd zijn. De werking van dit algoritme is op te splitsen in 3 grote delen:
- selecteren van de volgende todo's.
- selecteren van het volgende slot voor een gantry.
- beslissen van welke actie binnen de tick wordt uitgevoerd.
Deze 3 acties worden steeds uitgevoerd bij elke tick.

### Selecteren van de todo

De volgende todo voor een gantry wordt bepaald door voor elke gantry eerst te kijken indien deze het outputslot kan bereiken. Is dit zo en is er een slot dat de eerste item in de output sequence bevat dan zal het beginslot voor de actie worden omgezet in een integer array van de vorm: [ item id, slot id, item priority ] en het eindslot onder dezelfde vorm, met als slot id dan het output slot. Deze array wordt dan natuurlijk toegevoegd aan de todo lijst. Vervolgens wordt voor de gantries die nog een lege todo lijst hebben gekeken of ze het input slot kunnen bereiken. Is dit zo dan zal de eerste item worden toegekend aan het input slot en zal een array worden toegevoegd aan de todo van de gantry. Voor het eindSlot wordt [ item id, -1, item priority ] toegevoegd. Deze -1 kan in de volgende stap gededecteerd worden om te weten dat er nog een slot dient gekozen te worden. Wanneer een task uit de input of output sequentie is omgezet, wordt deze eruit verwijdert.

### Selecteren van volgende slot

Om het volgende slot te bepalen dat door een gantry moet bedient worden wordt de eerste actie uit zijn todo bekeken. Deze wordt nog niet verwijdert. Het verwijderen gebeurt maar wanneer de actie volledig voltooid is, dit om het einde van het algoritme te kunnen dedecteren. Er wordt hier eerst gekeken of de gantry het slot wel kan bereiken. Het kan zijn dat dit niet zo is bij het uitgraven. Het uitgraven zal gebeuren wanneer het slot van de volgende item begraven ligt. Het uitgraven zal acties toevoegen aan de todo van de gantry. Steeds met een volledig ingevulde begin actie gevolgd door een actie met slot id -1. Als dit slot id in de actie -1 is, zal een leeg slot gezocht worden om de item in te plaatsen. Bij zowel het uitgraven zal het onderste slot worden toegevoegd aan een lijst slots die dient genegeerd te worden bij het vinden van een nieuw leeg slot, ook een gevonden leeg slot wordt hierin opgeslagen. Er wordt steeds gekeken dat er geen item in een te negeren slot zit en dat we geen item op een te negeren slot plaatsen. 

### Actie kiezen

Voor de actie te kiezen wordt gebruik gemaakt van een voorspellende strategie. Er wordt eerst gekeken of we niet in de situatie zitten waar 1 gantry een volgend slot toegekend heeft gekregen, maar de andere niet. In dit geval zal de ene gantry zijn actie volledig voltooien. De andere zal blijven staan waar ze staat indien de safety distance niet wordt overschreden, anders zal ze zich verplaatsen om collision te vermijden. Indien beide kranen een volgende slot toegekend hebben gekregen zal gekeken worden of de safety distance niet wordt overschreden wanneer de kortste actue wordt uitgevoerd. Als dit zo is wordt deze uitgevoerd en zal de andere gantry bewegen naar zijn volgende slot gedurende de actie van de eerste. Zitten we wel in de situatie waar de 2 gantries zouden botsen zal de gantry met de korste uitvoeringstijd prioriteit krijgen en zijn actie volledig uitvoeren. De andere gantry zal reeds in de richting van zijn volgende slot bewegen, maar zal de eerste gantry ontwijken. Deze actie wordt uitgevoerd door steeds maar 1 tijdseenheid te bewegen. Dit werd zo beslist omdat het de simpelste maar toch meest robuuste methode was.

## Resultaat en verbetering

Dit algoritme genereerd een oplossing die valid is voor elke instantie en zorgt voor een kleine verbetering wanneer 2 kranen gebruikt worden. Het zou nog verder kunnen verbeterd worden door de niet bewegende kraan reeds een item te laten uitgraven. De beste strategie hiervoor zou zijn om de meest linkse kraan steeds een zo links mogelijk item te laten uitgraven en de meest rechtse kraan steeds een zo rechts mogelijk item. Op deze manier minimaliseren we mogelijke hindering. Door collision te vermijden door elke gantry steeds 1 tijdseenheid te laten bewegen zorgen we wel voor een potentieel iets langere uitvoeringstijd van het programma, maar door de simpliciteit van deze actie is dit bijna niet merkelijk. Een grotere dataset zou eventueel wel een merkbaar langere uitvoeringstijd veroorzaken.
