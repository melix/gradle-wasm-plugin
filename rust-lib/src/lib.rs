use std::collections::HashMap;
use std::hash::Hash;

#[no_mangle]
pub extern fn fibo(n: i64) -> i64 {
    let cache = &mut HashMap::new();
    fib(cache, n)
}

fn memoize<A, R, F> (cache: &mut HashMap<A, R>, func: F, arg: A) -> R where
    A: Eq + Hash + Clone,
    R: Clone,
    F: Fn(&mut HashMap<A, R>, A) -> R
{
    match cache.get(&arg).map(|x| x.clone()) {
        Some(result) => result,
        None => {
            let result = func(cache, arg.clone());
            cache.insert(arg, result.clone());
            result
        }
    }
}

fn fib(hm: &mut HashMap<i64, i64>, n: i64) -> i64 {
    match n {
        0 => 0,
        1 => 1,
        n => memoize(hm, fib, n -1) + memoize(hm, fib, n - 2)
    }
}
