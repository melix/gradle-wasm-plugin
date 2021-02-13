use std::collections::HashMap;
use std::hash::Hash;
use std::mem;
use md5::{Md5, Digest};
use md5::digest::{FixedOutput};
use std::os::raw::c_void;

#[no_mangle]
pub extern fn fibo(n: i64) -> i64 {
    let cache = &mut HashMap::new();
    fib(cache, n)
}

#[no_mangle]
pub extern fn allocate(size: usize) -> *mut c_void {
    let mut buffer = Vec::with_capacity(size);
    let pointer = buffer.as_mut_ptr();
    mem::forget(buffer);

    pointer as *mut c_void
}

#[no_mangle]
pub extern fn deallocate(pointer: *mut c_void, capacity: usize) {
    unsafe {
        let _ = Vec::from_raw_parts(pointer, 0, capacity);
    }
}

#[no_mangle]
pub extern fn process(bytes: *const u8, len: usize) -> *const c_void {
    let data: &[u8] = unsafe { std::slice::from_raw_parts(bytes, len) };
    let mut hasher = Md5::new();
    hasher.update(data);
    let result = hasher.finalize_fixed();
    let pointer = result
        .to_vec()
        .as_ptr();
    mem::forget(pointer);

    pointer as *const c_void
}

fn memoize<A, R, F>(cache: &mut HashMap<A, R>, func: F, arg: A) -> R where
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
        n => memoize(hm, fib, n - 1) + memoize(hm, fib, n - 2)
    }
}
