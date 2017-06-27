# directionalsurvey

A [reagent](https://github.com/reagent-project/reagent) application designed to ... well, that part is up to you.

Step1: login using an abitrary name. 
Step2: You can change the deviation column number. 
Step3: You can then change to another user and do step2 again.
Step4: Slide to go back to history. 

## Development Mode

### Run application:

```
lein clean
lein figwheel dev
```

Figwheel will automatically push cljs changes to the browser.

Wait a bit, then browse to [http://localhost:3449](http://localhost:3449).

## Production Build

```
lein clean
lein cljsbuild once min
```
