Тестирование кеша


        addChannelButton = new Button();
        addChannelButton.setText( "+ Channels" );
        addChannelButton.setOnAction( e -> {

            final long[] start = new long[1];

            final Channel[] c1 = new Channel[1];

            ArrayList<String> names = new ArrayList<>( 10_000 );

            /**
             * [нагрузочное тестирование] в главном потоке
             * Добавление и чтение 10 тыс объектов в пустой кеш:
             * Добавление и чтение 10 тыс объектов повторно: 37 сек
             * Разогрев кэша с 20 тыс объектов: 28 сек
             * [!] Разогрев кеша с 30 тыс объектов без воспроизведения нод занял 7 сек
             *     Разогрев кеша с 30 тыс объектов с воспроизведением узлов: 43 сек
             *     Разогрев кеша с 10 тыс объектов с воспроизведением узлов: 4 сек
             * В связи с этим - вопрос о целесообразности наличия кеша L1
             *
             * todo
             * [тест L1 и L2] сравнить время выборки объектов, кода они достаются из L2 и l1
             * Создать и закешировать в пустой кеш 10к каналов
             *      - сгенерить имя кнала
             *      - закешить канал
             *      - добавить имя в список
             * Сгенерить список комплексных запросов
             * Закешировать эти запросы
             * Прогнать 10к комплексных запросов на выборку с фиксацией времени (тест L1)
             * Удалить комплексные запросы из кеша - очистить L1. Либо просто заблокировать восстановление узлов при старте.
             * Прогнать 10к комплексных запросов (тест L2)
             *
             *
             *
             * [нагрузочное тестирование] запись в отдельном потоке
             * Добавление и чтение 10 тыс объектов в пустой кеш: 8 - 10 cек
             * Добавление в непустой кеш с одновременным чтением в отдельном потоке с печатью в консоль: 44 сек
             * Добавление в непустой кеш с одновременным извлечением объектов в отдельном потоке без печати в консоль: 34 сек
             * Добавление в непустой кеш с одновременным извлечением объектов в отдельном потоке с выводом в консоль: 76 сек (изначально в кеше было 20 тыс объектов)
             *      Провалов в выводе или исключениий НЕ было, как в случае с выборкой через Storage.getInstance().getRequests().parallelStream()
             *
             * [!] при большом количестве каналов нужно подождать завершения разогрева кеша,
             *      после чего можно запускать тест. Видимо, происходит переполнения очереди
             *
             * [!] можно написать тест, при котором в кеш пишется 10К объектов
             * и одновременно читается десяток известных объектов (которые заведомо лежат в кеше) в бесконечном цикле.
             * Запускать тест только, когда кеш разогрет.
             * Попробовтаь вместо использованной здесь конструкции Storage.getInstance().getRequests().parallelStream()
             *      заюзать CacheService.get("") - так, чтобы нода заведомо существовала и НЕ существовала.
             *      Посмотреть, отвалится ли тест, ведь сейчас в кеш-машине имплементирован простой stream.
             * Также, можно поменять структуру данных с synchronizedSortedSet на какую-нибудь другую, напр, TreeSet или HashSet (или, вообще, List)
             *
             *
             * todo
             * [!] Сделать запись кеша в отдельном потоке
             *
             */

            AtomicBoolean latch = new AtomicBoolean( true );

            // write channels test
            Thread threadWrite = new Thread( () -> {
                int ind = 0;
                String s = "names.add(\"";

                start[0] = System.currentTimeMillis() / 1000;

                for ( int i = 0; i < 10_000; i++ ) {
                    c1[0] = new Channel( getRndChannelName() );
                    CacheService.set( c1[0].getChannelId(), c1[0] );

                    s += c1[0].channelId + ",";

                    if ( ++ind == 10 ){
                        names.add( s.replaceAll( ",$", "\");" ) );
                        s = "names.add(\"";
                        ind = 0;
                    }
                }

                long end = System.currentTimeMillis() / 1000;
                long res = end - start[0];

                names.stream().forEach( System.out::println );

                System.out.println( "Writing time: " + res );
                latch.set( false );
            } );
            // threadWrite.start();

            // тест записи 2
            Thread threadWrite2 = new Thread( () -> {
                System.out.println( "генерируем каналы" );
                long startT = System.currentTimeMillis() / 1000;
                TestHelper helper = new TestHelper();
                helper.requests.stream().forEach( r -> Arrays.stream( r.split( "," ) ).map( Channel::new ).forEach( channel -> { CacheService.set( channel.getChannelId(),channel ); } ) );
                long end = System.currentTimeMillis() / 1000;
                long res = end - startT;
                System.out.println( "Writing time: " + res );
            });
            // threadWrite2.start();

            // read channels test
            Thread threadRead = new Thread( () -> {
                System.out.println("Start reading test");
                long startReading = System.currentTimeMillis() / 1000;

                // [!] поток читает из ранее сохраненных данных, а не из тех, которые кешируются в данный момент

                // вариант 1 - явно перелопачиваем реквесты.
                // В таком варианте возникали пустые выводы, а при использовании просто stream() - concurentModificationException
                // Storage.getInstance().getRequests().parallelStream().map( key -> CacheService.get( key ) ); //.filter( Objects::nonNull ).forEach( item -> System.out.println(item) );

                // первые 5 ключей закешированы в L1, вторые 5 - только в L2.
                // Однако, Это НЕ вынудит сервис использовать разные уровни кеша, т.к. при разогреве восстанавливаются все ноды
                // String[] arr = new String[]{"bghqdxhryq2","dmotpehklx8","ganmvprqpn23","knognazpmb16","lqxpszkdjc6","nvguevfbbl25","ovmxfphhkz24","pgoajgweeh25","rgkwbbbobl7","vfqhvtgclr0"};

                TestHelper helper = new TestHelper();


                // вариант 2
                // while ( latch.get() )
                /**
                 * Модель хранилища: synchronizedSortedSet
                 * В кеше: 10к объектов
                 * каждый запрос содержит 10 каналов
                 * чтение из кеша L1
                 * 1_0 циклов: 3 сек
                 * 1_00 циклов: 9 сек
                 * 1_000 циклов: 94 сек (87 сек при использовании ConcurrentSkipListSet, 104 сек для TreeSet, 185 сек для HashSet)
                 * 10_000 циклов: 948 сек
                 * 100_000 циклов: ? сек
                 * чтение из кеша L2
                 * 1_0 циклов чтения: 1
                 * 1_00 циклов чтения: 2
                 * 1_000 циклов чтения: 17 сек
                 * 10_000 циклов чтения: 171 сек
                 * 100_000 циклов чтения: 1743 сек
                 * Видим, что L1 кеш при данной реализации совершенно не работает и даже снижает показатели доступа на порядок
                 *
                 * чтение из кеша L2 (после того, как что-то поменялось. Видимо, причина в пустом кеше)
                 * 1_0 циклов чтения:  7 сек
                 * 1_00 циклов чтения:  по идее, д.б. 70
                 * 1_000 циклов чтения: 700
                 * 10_000 циклов чтения: 8710 (~2,5 часа) [!] после теста кеш был сохранен и он оказался пустым
                 */
                for ( int i = 0; i < 100; i++ )
                {
                    // System.out.println("проход " + i);
                    helper.requests.stream().forEach( key -> {
                        ArrayList<Channel> c = CacheService.get( key );
                        if ( c.size() > 0 )
                        {
                            // System.out.println( "Stream " + c.get( 0 ).getChannelId() );
                            // System.out.println(c);
                        }
                    } );
                }

                long end = System.currentTimeMillis() / 1000;
                long elapsed = end - startReading;

                System.out.println( "Reading time, сек: " + elapsed );
            } );
            threadRead.start();

        } );
        
      private static char[] alfa = new char[]{'a','b','c','d','e','f','g','h','i','j','k','l','m','n','o','p','q','r','s','t','u','v','w','x','y','z'};
         private static Random random = new Random(  );
     
         private String getRndChannelName()
         {
             StringJoiner joiner = new StringJoiner( "" );
     
             for ( int i = 0; i < 10; i++ ) {
                 joiner.add( Character.toString( alfa[getRndInt()] ) );
             }
     
             joiner.add( Integer.toString( getRndInt() ) );
     
             return joiner.toString();
         }
     
         private int getRndInt()
         {
             return random.nextInt( 26 - 0 );
         }   
        
        

        getNodeButton = new Button();
        getNodeButton.setText( "get Channel" );
        getNodeButton.setOnAction( e -> {
            Channel channel = null;
            try {
                channel = CacheService.get( "Channel D2" ).get( 0 );
                System.out.println("Got " + channel.getChannelId() + ", expDate " + channel.getExpirationDate());
            } catch ( Exception e1 ) {
                System.out.println("not found");
            }
        } );
        
        // ---- Rx --------
         Предпочтительно Observable.fromCallable
                 * https://habrahabr.ru/post/265269/
                 * https://habrahabr.ru/post/265583/
                 * https://habrahabr.ru/post/265727/