package org.s3a.hh;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.function.Predicate;
import java.util.stream.LongStream;

public class Exercise {
    private static Logger logger = LoggerFactory.getLogger(Exercise.class);

    public static void main(String[] args) {
        if(args == null || args.length != 2) {
            System.out.println("Usage is: Exercise # ClassName\n # is the maximum value to test.\n  ClassName is the fully qualified class implementing the org.s3a.Secret interface.");
            System.exit(1);
        }
        Long maxPrime = 0L;
        try {
            maxPrime = Long.valueOf(args[0]);
        } catch (NumberFormatException ex) {
            System.out.println("Usage is: Exercise # ClassName\n # is the maximum value to test.\n  ClassName is the fully qualified class implementing the org.s3a.Secret interface.");
            System.exit(1);
        }
        if(maxPrime < 1) {
            logger.error("Please specify a positive integer");
            System.exit(1);
        }
        // Cheating a bit and burying a System.exit() because this needs to be final.
        final Secret secret = getSecret(args[1]);
        if(secret == null) {
            logger.error("Error instantiating class " + args[1]);
            System.exit(1);
        }

        Collection<Long> primeNumbers = getPrimeNumbers(maxPrime);

        boolean isAdditive = isFuncationAdditiveForAllValues(secret, primeNumbers);
        logger.info("The function is " + (isAdditive ? "" : "not ") + "additive.");
        System.exit(isAdditive ? 0 : 1);
    }

    private static Secret getSecret(String arg) {
        try {
            return (Secret) Class.forName(arg).newInstance();
        } catch (ClassNotFoundException | IllegalAccessException | InstantiationException | ClassCastException ex) {
            logger.error("Could not instantiate class " + arg);
            System.exit(1);
        }
        return null;
    }

    private static Collection<Long> getPrimeNumbers(Long maxPrime) {
        Collection<Long> primeNumbers = new ConcurrentSkipListSet<>();
        Predicate<Long> isPrime = new Predicate<Long>() {
            @Override
            public boolean test(Long o) {
                // Normally it would be dangerous to autobox this and then call .intValue(), but we know that the input will be a positive integer
                Double sqrt = Math.sqrt(o.doubleValue());
                return LongStream.rangeClosed(2, sqrt.intValue()).noneMatch(i -> o % i == 0);
            }
        };
        LongStream.range(1, maxPrime).parallel().filter(isPrime::test).forEach(primeNumbers::add);
        if(logger.isDebugEnabled()) {
            for (Long primeNumber : primeNumbers) {
                logger.debug(primeNumber + " is Prime");
            }
        }
        return primeNumbers;
    }

    private static boolean isFuncationAdditiveForAllValues(final Secret secret, final Collection<Long> primeNumbers) {
        Predicate<Long> additivePredicate = new Predicate<Long>() {
            @Override
            public boolean test(Long integer) {
                // No point in doing this n times.
                Long secretValue = secret.doSecret(integer);
                return primeNumbers.parallelStream().allMatch(i -> secret.doSecret(i + integer) == secret.doSecret(i) + secretValue);
            }
        };
        return primeNumbers.parallelStream().allMatch(additivePredicate);
    }
}
