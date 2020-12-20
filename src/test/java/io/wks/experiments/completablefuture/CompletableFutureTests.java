package io.wks.experiments.completablefuture;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import org.json.JSONObject;
import org.junit.Test;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.assertThat;

public class CompletableFutureTests {

    private static class IPInfo {
        private final String city;
        private final String country;

        public IPInfo(String city, String country) {
            this.city = city;
            this.country = country;
        }
    }

    private static class MessageHolder {
        private String message = null;
    }

    private IPInfo loadIPInfo() throws IOException {
        var client = new OkHttpClient();
        var request = new Request.Builder().url("https://ipinfo.io/json").build();
        var response = client.newCall(request).execute();
        var json = new JSONObject(response.body().string());
        return new IPInfo(json.getString("city"), json.getString("country"));
    }

    private CompletableFuture<String> getCountry() {
        CompletableFuture<String> completableFuture = new CompletableFuture<>();

        Executors.newCachedThreadPool().submit(() -> {
            try {
                completableFuture.complete(loadIPInfo().country);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

        return completableFuture;
    }

    private CompletableFuture<String> getCity() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return loadIPInfo().city;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Test
    public void completedFutureReturnsValueImmediately() throws ExecutionException, InterruptedException {
        assertThat(CompletableFuture.completedFuture("Hello").get()).isEqualTo("Hello");
    }

    @Test
    public void getFutureBlocksForResult() throws ExecutionException, InterruptedException {
        assertThat(getCountry().get()).isEqualTo("AE");
    }

    @Test
    public void runAsyncAcceptsARunnableThereforeDoesNotReturnAValue() throws ExecutionException, InterruptedException {
        var messageHolder = new MessageHolder();

        var future = CompletableFuture.runAsync(() -> {
            messageHolder.message = "Hi";
        });

        future.get();
        assertThat(messageHolder.message).isEqualTo("Hi");
    }

    @Test
    public void supplyAsyncAcceptsASupplierAndThereforeReturnsAValue() throws ExecutionException, InterruptedException {
        var messageHolder = new MessageHolder();

        var future = CompletableFuture.supplyAsync(() -> {
            messageHolder.message = "Hi";
            return messageHolder;
        });

        assertThat(future.get().message).isEqualTo("Hi");
    }

    @Test
    public void thenApplyAppliesAFunctionToTheFuturesResultAndThenReturnsTheNewResult() throws ExecutionException, InterruptedException {
        // thenApply is like map
        var future = getCountry().thenApply(country -> String.format("You live in %s", country));
        assertThat(future.get()).isEqualTo("You live in AE");
    }

    @Test
    public void thenAcceptAppliesAFunctionToTheFuturesResultButReturnsNothing() throws ExecutionException, InterruptedException {
        var messageHolder = new MessageHolder();

        getCountry()
                .thenAccept(country -> messageHolder.message = country)
                .get();

        assertThat(messageHolder.message).isEqualTo("AE");
    }

    @Test
    public void thenComposeComposesAnObjectUsingMultipleFutures() throws ExecutionException, InterruptedException {
        // thenCompose is like flatMap

        var future = getCity().thenCompose(city -> CompletableFuture.completedFuture(String.format("You live in %s", city)));

        assertThat(future.get()).isEqualTo("You live in Dubai");
    }

    @Test
    public void thenCombineCombinesTheResultsOfMultipleFutures() throws ExecutionException, InterruptedException {
        var future = getCity().thenCombine(getCountry(), (city, country) -> String.format("You live in %s, %s", city, country));

        assertThat(future.get()).isEqualTo("You live in Dubai, AE");
    }
}
